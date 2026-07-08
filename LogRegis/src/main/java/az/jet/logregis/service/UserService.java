package az.jet.logregis.service;

import az.jet.logregis.dao.entity.UserEntity;
import az.jet.logregis.dao.repository.UserRepository;
import az.jet.logregis.dto.request.*;
import az.jet.logregis.dto.response.ActivateUserResponse;
import az.jet.logregis.dto.response.UserRegisterResponse;
import az.jet.logregis.dto.response.UserValidateResponse;
import az.jet.logregis.feignclients.VerifyClient;
import az.jet.logregis.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final VerifyClient verifyClient;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String KEY_PREFIX = "token:";

    public UserRegisterResponse register(UserRegisterRequest dto) {
        var entitySt = userMapper.toEntity(dto);
        entitySt.setPassword(passwordEncoder.encode(entitySt.getPassword()));
        var entity = userRepository.save(entitySt);
        verifyClient.sendOtp(
                new OtpRequest(
                        entity.getEmail()
                )
        );
        return userMapper.toResponse(entity, "OTP code was sent to your EMAIL address");
    }

//    public UserValidateResponse login(UserValidateRequest dto) {
//        var entity = userRepository.findByUsername(dto.getEmail()).orElseThrow(() -> new RuntimeException("Not found!"));
//        if (!entity.getIsAuth()) {
//            return UserValidateResponse.builder()
//                    .message("Not verified yet! Check your email for OTP!")
//                    .build();
//        }
//        if (!passwordEncoder.matches(dto.getPassword(), entity.getPassword())) {
//            return UserValidateResponse.builder()
//                    .message("Invalid username or password")
//                    .build();
//        }
//        UserDetails userDetails = User.builder()
//                .username(entity.getUsername())
//                .password(entity.getPassword())
//                .authorities("USER")
//                .build();
//        String accessToken = jwtService.generateAccessToken(userDetails);
//        String refreshToken = jwtService.generateRefreshToken(userDetails);
//        redisTemplate.delete(KEY_PREFIX);
//        redisTemplate.opsForValue().set(KEY_PREFIX,accessToken);
//        redisTemplate.opsForValue().set(KEY_PREFIX,refreshToken);
//        return UserValidateResponse.builder()
//                .message("Logged in successfully")
//                .accessToken(accessToken)
//                .refreshToken(refreshToken)
//                .build();
//    }

    public UserValidateResponse refresh(RefreshTokenRequest dto) {
        String refreshToken = dto.getRefreshToken();

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);
        var entity = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetails userDetails = User.builder()
                .username(entity.getUsername())
                .password(entity.getPassword())
                .authorities("USER")
                .build();

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new RuntimeException("Refresh token expired");
        }

        String newAccessToken = jwtService.generateAccessToken(userDetails);

        return UserValidateResponse.builder()
                .message("Access token refreshed")
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public void activate(ActivateUserRequest dto) {
        UserEntity user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsAuth(true);
        userRepository.save(user);
    }
}
package az.jet.logregis.service;

import az.jet.logregis.dao.entity.UserEntity;
import az.jet.logregis.dao.repository.UserRepository;
import az.jet.logregis.dto.request.*;
import az.jet.logregis.dto.response.ActivateUserResponse;
import az.jet.logregis.dto.response.RefreshTokenResponse;
import az.jet.logregis.dto.response.UserRegisterResponse;
import az.jet.logregis.dto.response.UserLoginResponse;
import az.jet.logregis.feignclients.VerifyClient;
import az.jet.logregis.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
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
    @Transactional
    public UserLoginResponse login(UserLoginRequest dto) {
        var entity = userRepository.findByEmail(dto.getEmail()).orElseThrow(() -> new RuntimeException("Not found!"));
        if (!entity.getIsAuth()) {
            return UserLoginResponse.builder()
                    .message("Not verified yet!")
                    .build();
        }
        if (!passwordEncoder.matches(dto.getPassword(), entity.getPassword())) {
            return UserLoginResponse.builder()
                    .message("Invalid username or password")
                    .build();
        }
        UserDetails userDetails = User.builder()
                .username(entity.getUsername())
                .password(entity.getPassword())
                .authorities("USER")
                .build();
        String accessKey = "access" + KEY_PREFIX;
        String refreshKey = "refresh" + KEY_PREFIX;
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);

        redisTemplate.opsForValue().set(accessKey,accessToken);
        redisTemplate.opsForValue().set(refreshKey,refreshToken);
        return UserLoginResponse.builder()
                .message("Logged in successfully")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    @Transactional
    public RefreshTokenResponse refresh(RefreshTokenRequest dto) {
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
        if (!jwtService.isTokenValid(refreshToken,userDetails)){
            throw new RuntimeException("Refresh token expired");
        };
        String accessKey = "refresh" + KEY_PREFIX;
        String savedRefreshToken = redisTemplate.opsForValue().get(accessKey);
        if (refreshToken.equals(savedRefreshToken)) {
            String newAccessKey = "access" + KEY_PREFIX;
            String newRefreshKey = "refresh" + KEY_PREFIX;
            String newAccessToken = jwtService.generateAccessToken(userDetails).toString();
            String newRefreshToken = jwtService.generateRefreshToken(userDetails).toString();
            redisTemplate.opsForValue().set(newAccessKey,newAccessToken);
            redisTemplate.opsForValue().set(newRefreshKey,newRefreshToken);
            return RefreshTokenResponse.builder()
                    .message("Access token refreshed")
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .build();
        }
        return RefreshTokenResponse.builder()
                .message("Invalid refresh token!")
                .build();
    }

    @Transactional
    public ActivateUserResponse activate(ActivateUserRequest dto) {
        UserEntity user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        var activateResponse = verifyClient.verifyOtp(dto);
        if (activateResponse.getIsVerified()) {
        user.setIsAuth(true);
        userRepository.save(user);
        return new ActivateUserResponse(activateResponse.getMessage(),activateResponse.getIsVerified());
        }
        return new ActivateUserResponse(activateResponse.getMessage(),activateResponse.getIsVerified());
    }
    @Transactional
    public String verifyToken(VerifyTokenRequest dto) {
        String accessKey = "access" + KEY_PREFIX;
        String accessToken = redisTemplate.opsForValue().get(accessKey);
        log.info("Request token: [{}]", dto.getToken());
        log.info("Redis token:   [{}]", accessToken);
        assert accessToken != null;
        if(accessToken.equals(dto.getToken())) {
            return "Success";
        }
        return "Failed";
    }
}
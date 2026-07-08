package az.jet.otpservice.service;

import az.jet.otpservice.dto.request.ActivateUserRequest;
import az.jet.otpservice.dto.request.OtpRequest;
import az.jet.otpservice.dto.response.OtpResponse;
import az.jet.otpservice.dto.response.ActivateUserResponse;
import az.jet.otpservice.feignclients.UserClient;
import az.jet.otpservice.mapper.OtpMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpService {
    private final OtpMapper otpMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "otp";
    private final MailService mailService;
    private final UserClient userClient;

    public OtpResponse sendOtp(OtpRequest dto) {
        String key = KEY_PREFIX + dto.getEmail();
        redisTemplate.delete(key);
        Random random = new Random();
        String otpCode = String.format("%06d",random.nextInt(1_000_000));
        redisTemplate.opsForValue().set(key,otpCode, Duration.ofMinutes(5));
        mailService.send(dto.getEmail(),otpCode);
        return otpMapper.toResponse("Otp code has been sent to your EMAIL address!" + dto.getEmail());
    }

    public ActivateUserResponse activatedUser(ActivateUserRequest dto) {
        String key = KEY_PREFIX + dto.getEmail();  // ← Исправлено: добавляем email
        Object storedCodeObj = redisTemplate.opsForValue().get(key);

        if (storedCodeObj == null) {
            return new ActivateUserResponse("OTP not found or expired!", false);
        }

        String storedCode = storedCodeObj.toString();

        if (storedCode.equals(dto.getOtpCode())) {
            redisTemplate.delete(key);
            try {
                userClient.activateUser(dto);
                log.info("User activated: {}", dto.getEmail());
                return new ActivateUserResponse("OTP verified and user activated successfully!", true);
            } catch (Exception e) {
                log.error("Failed to activate user: {}", e.getMessage());
                return new ActivateUserResponse("Failed to activate user: " + e.getMessage(), false);
            }
        }
        return new ActivateUserResponse("Wrong OTP!", false);
    }
}
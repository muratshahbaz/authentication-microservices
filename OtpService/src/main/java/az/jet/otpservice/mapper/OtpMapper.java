package az.jet.otpservice.mapper;

import az.jet.otpservice.dto.response.OtpResponse;
import az.jet.otpservice.dto.response.ActivateUserResponse;
import org.springframework.stereotype.Component;

@Component
public class OtpMapper {

    public OtpResponse toResponse(String message) {
        return OtpResponse
                .builder()
                .message(message)
                .build();
    }
    public ActivateUserResponse toVerifyResponse(String message) {
        return ActivateUserResponse
                .builder()
                .message(message)
                .build();
    }
}
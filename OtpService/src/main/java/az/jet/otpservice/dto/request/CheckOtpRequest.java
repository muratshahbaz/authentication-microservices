package az.jet.otpservice.dto.request;

public record CheckOtpRequest(
        String email,
        String code
) {
}
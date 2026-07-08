package az.jet.logregis.feignclients;
import az.jet.logregis.dto.request.OtpRequest;
import az.jet.logregis.dto.request.ActivateUserRequest;
import az.jet.logregis.dto.response.ActivateUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(
        name = "otp-service",
        url = "${services.otp.url}"
)
public interface VerifyClient {


    @PostMapping("/api/v1/otp/sendOtp")
    void sendOtp(@RequestBody OtpRequest request);

    @PostMapping("/api/v1/otp/verify")  // ← Исправлено название метода
    ActivateUserResponse verifyOtp(@RequestBody ActivateUserRequest request);
}
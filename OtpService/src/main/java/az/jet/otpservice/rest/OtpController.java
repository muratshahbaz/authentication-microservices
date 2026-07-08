package az.jet.otpservice.rest;
import az.jet.otpservice.dto.request.ActivateUserRequest;
import az.jet.otpservice.dto.request.OtpRequest;
import az.jet.otpservice.dto.response.OtpResponse;
import az.jet.otpservice.dto.response.ActivateUserResponse;
import az.jet.otpservice.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/otp")
@RequiredArgsConstructor
public class OtpController {
    private final OtpService otpService;

    @PostMapping("/sendOtp")
    public OtpResponse sendOtp(@RequestBody OtpRequest dto) {
        return otpService.sendOtp(dto);
    }

    @PostMapping("/verify")
    public ActivateUserResponse activateUser(@RequestBody ActivateUserRequest dto) {
        return otpService.activatedUser(dto);
    }
}
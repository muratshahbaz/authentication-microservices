package az.jet.otpservice.feignclients;

import az.jet.otpservice.dto.request.ActivateUserRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "user-service",
        url = "${services.user.url}"
)
public interface UserClient {

    @PostMapping("/api/v1/us/activate")
    void activateUser(
            @RequestBody ActivateUserRequest request
    );
}
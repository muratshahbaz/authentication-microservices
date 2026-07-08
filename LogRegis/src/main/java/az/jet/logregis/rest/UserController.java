package az.jet.logregis.rest;

import az.jet.logregis.dto.request.ActivateUserRequest;
import az.jet.logregis.dto.request.RefreshTokenRequest;
import az.jet.logregis.dto.request.UserRegisterRequest;
import az.jet.logregis.dto.response.UserRegisterResponse;
import az.jet.logregis.dto.response.UserValidateResponse;
import az.jet.logregis.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/us")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public UserRegisterResponse register(@RequestBody @Valid UserRegisterRequest dto) {
        return userService.register(dto);
    }

//    @PostMapping("/verify")
//    public UserValidateResponse verify(@RequestBody @Valid UserValidateRequest dto) {
//        return userService.login(dto);
//    }

    @PostMapping("/refresh")
    public UserValidateResponse refresh(@RequestBody RefreshTokenRequest request) {
        return userService.refresh(request);
    }
    @PostMapping("/activate")
    public void activate(
            @RequestBody ActivateUserRequest dto
    ){

        userService.activate(dto);

    }

}
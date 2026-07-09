package az.jet.logregis.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserLoginResponse {
     String accessToken;
     String refreshToken;
     String message;
}
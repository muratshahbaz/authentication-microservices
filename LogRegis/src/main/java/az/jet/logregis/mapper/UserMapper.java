package az.jet.logregis.mapper;

import az.jet.logregis.dao.entity.UserEntity;
import az.jet.logregis.dto.request.UserRegisterRequest;
import az.jet.logregis.dto.response.UserRegisterResponse;
import org.springframework.stereotype.Component;


@Component
public class UserMapper {

    public UserEntity toEntity(UserRegisterRequest dto) {
        return UserEntity
                .builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .age(dto.getAge())
                .email(dto.getEmail())
                .build();
    }
    public UserRegisterResponse toResponse(UserEntity entity,String message) {
        return UserRegisterResponse
                .builder()
                .id(entity.getId())
                .message(message)
                .build();
    }
}
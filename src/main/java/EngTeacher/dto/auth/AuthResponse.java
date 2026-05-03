package EngTeacher.dto.auth;

import EngTeacher.model.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private UserDto user;

    @Data
    @Builder
    public static class UserDto {
        private String id;
        private String name;
    }

    public static AuthResponse of(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .user(UserDto.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .build())
                .build();
    }
}

package EngTeacher.dto.auth;

import EngTeacher.model.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private User user;
}

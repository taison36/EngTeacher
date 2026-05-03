package EngTeacher.dto.auth;

import lombok.Data;

@Data
public class AuthRequest {
    private String name;
    private String password;
}

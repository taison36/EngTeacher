package EngTeacher.controller;

import EngTeacher.dto.auth.AuthRequest;
import EngTeacher.dto.auth.AuthResponse;
import EngTeacher.exceptions.ImproperApiUsageException;
import EngTeacher.model.User;
import EngTeacher.security.JwtService;
import EngTeacher.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody AuthRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ImproperApiUsageException("Name is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new ImproperApiUsageException("Password must be at least 6 characters");
        }

        Optional<User> existing = userService.findByName(request.getName());
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Name already taken");
        }

        User user = userService.createUser(request.getName(), request.getPassword());
        String token = jwtService.generateToken(user.getId());
        return AuthResponse.builder()
                .token(token)
                .user(user)
                .build();
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        User user = userService.findByName(request.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!userService.checkPassword(user, request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId());
        return AuthResponse.builder()
                .token(token)
                .user(user)
                .build();
    }
}

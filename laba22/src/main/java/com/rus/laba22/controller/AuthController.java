package com.rus.laba22.controller;

import com.rus.laba22.model.AppUser;
import com.rus.laba22.model.UserRole;
import com.rus.laba22.repository.AppUserRepository;
import com.rus.laba22.service.AuthTokenService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    private static final Pattern STRONG_PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-={}\\[\\]|:;\"'<>,.?/]).{8,}$");

    public AuthController(AppUserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuthTokenService authTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authTokenService = authTokenService;
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Size(min = 8, max = 100) String password,
            UserRole role
    ) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        if (!STRONG_PASSWORD_PATTERN.matcher(request.password()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must be at least 8 characters and contain a digit and a special symbol");
        }

        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role() != null ? request.role() : UserRole.USER);

        AppUser saved = userRepository.save(user);
        saved.setPasswordHash(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}

    public record LoginResponse(String token) {}

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = authTokenService.generateToken(user.getUsername());
        return new LoginResponse(token);
    }

    @GetMapping("/csrf")
    public ResponseEntity<?> getCsrfToken(CsrfToken token) {
        return ResponseEntity.ok().body(java.util.Map.of(
                "headerName", token.getHeaderName(),
                "parameterName", token.getParameterName(),
                "token", token.getToken()
        ));
    }
}


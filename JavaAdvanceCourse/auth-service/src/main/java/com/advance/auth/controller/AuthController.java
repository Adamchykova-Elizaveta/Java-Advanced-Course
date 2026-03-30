package com.advance.auth.controller;

import com.advance.auth.dto.AuthRequest;
import com.advance.auth.dto.AuthResponse;
import com.advance.auth.dto.CredentialsDto;
import com.advance.auth.dto.TokenRequest;
import com.advance.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody CredentialsDto dto) {
        authService.saveCredentials(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody TokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getToken()));
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validate(@Valid @RequestBody TokenRequest request) {
        return ResponseEntity.ok(Map.of("valid", authService.validateToken(request.getToken())));
    }
}
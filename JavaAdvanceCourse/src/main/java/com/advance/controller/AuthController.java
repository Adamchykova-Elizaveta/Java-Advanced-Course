package com.advance.controller;

import com.advance.dto.AuthRequest;
import com.advance.dto.AuthResponse;
import com.advance.dto.CredentialsDto;
import com.advance.dto.TokenRequest;
import com.advance.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
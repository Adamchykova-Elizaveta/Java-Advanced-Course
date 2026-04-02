package com.advance.authservice.service;

import com.advance.authservice.dto.AuthRequest;
import com.advance.authservice.dto.AuthResponse;
import com.advance.authservice.dto.CredentialsDto;
import com.advance.authservice.entity.Credentials;
import com.advance.authservice.exception.EntityNotFoundException;
import com.advance.authservice.repository.CredentialsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CredentialsRepository credentialsRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void saveCredentials(CredentialsDto dto) {
        if (credentialsRepository.existsByLogin(dto.getLogin())) {
            throw new IllegalArgumentException("Login already exists: " + dto.getLogin());
        }
        if (credentialsRepository.existsByUserId(dto.getUserId())) {
            throw new IllegalArgumentException("Credentials already exist for user: " + dto.getUserId());
        }
        Credentials credentials = Credentials.builder()
                .userId(dto.getUserId())
                .login(dto.getLogin())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .build();
        credentialsRepository.save(credentials);
    }

    public AuthResponse login(AuthRequest request) {
        Credentials credentials = credentialsRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new EntityNotFoundException("Credentials", 0L));

        if (!passwordEncoder.matches(request.getPassword(), credentials.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(credentials.getUserId(), credentials.getRole()))
                .refreshToken(jwtService.generateRefreshToken(credentials.getUserId(), credentials.getRole()))
                .build();
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        Long userId = jwtService.getUserId(refreshToken);
        com.advance.authservice.entity.Role role = jwtService.getRole(refreshToken);

        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(userId, role))
                .refreshToken(jwtService.generateRefreshToken(userId, role))
                .build();
    }

    public boolean validateToken(String token) {
        return jwtService.isTokenValid(token);
    }
}

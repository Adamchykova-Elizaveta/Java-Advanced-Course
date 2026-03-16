package com.advance.service;

import com.advance.dto.AuthRequest;
import com.advance.dto.AuthResponse;
import com.advance.dto.CredentialsDto;
import com.advance.entity.Credentials;
import com.advance.entity.Role;
import com.advance.exception.EntityNotFoundException;
import com.advance.repository.CredentialsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private CredentialsRepository credentialsRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private Credentials credentials;

    @BeforeEach
    void setUp() {
        credentials = new Credentials();
        credentials.setId(1L);
        credentials.setUserId(1L);
        credentials.setLogin("anna");
        credentials.setPassword("hashed_password");
        credentials.setRole(Role.ADMIN);
    }

    @Test
    void saveCredentials_ShouldSave_WhenLoginNotExists() {
        when(credentialsRepository.existsByLogin("anna")).thenReturn(false);
        when(credentialsRepository.existsByUserId(1L)).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");

        CredentialsDto dto = new CredentialsDto("anna", "password123", 1L, Role.ADMIN);
        authService.saveCredentials(dto);

        verify(credentialsRepository).save(any(Credentials.class));
    }

    @Test
    void saveCredentials_ShouldThrow_WhenLoginExists() {
        when(credentialsRepository.existsByLogin("anna")).thenReturn(true);

        CredentialsDto dto = new CredentialsDto("anna", "password123", 1L, Role.ADMIN);

        assertThatThrownBy(() -> authService.saveCredentials(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Login already exists");
    }

    @Test
    void saveCredentials_ShouldThrow_WhenUserAlreadyHasCredentials() {
        when(credentialsRepository.existsByLogin("anna")).thenReturn(false);
        when(credentialsRepository.existsByUserId(1L)).thenReturn(true);

        CredentialsDto dto = new CredentialsDto("anna", "password123", 1L, Role.ADMIN);

        assertThatThrownBy(() -> authService.saveCredentials(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credentials already exist");
    }

    @Test
    void login_ShouldReturnTokens_WhenCredentialsValid() {
        when(credentialsRepository.findByLogin("anna")).thenReturn(Optional.of(credentials));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
        when(jwtService.generateAccessToken(1L, Role.ADMIN)).thenReturn("access_token");
        when(jwtService.generateRefreshToken(1L, Role.ADMIN)).thenReturn("refresh_token");

        AuthResponse response = authService.login(new AuthRequest("anna", "password123"));

        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_token");
    }

    @Test
    void login_ShouldThrow_WhenLoginNotFound() {
        when(credentialsRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new AuthRequest("unknown", "password123")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void login_ShouldThrow_WhenPasswordInvalid() {
        when(credentialsRepository.findByLogin("anna")).thenReturn(Optional.of(credentials));
        when(passwordEncoder.matches("wrong", "hashed_password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new AuthRequest("anna", "wrong")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid password");
    }

    @Test
    void refresh_ShouldReturnNewTokens_WhenTokenValid() {
        when(jwtService.isTokenValid("refresh_token")).thenReturn(true);
        when(jwtService.getUserId("refresh_token")).thenReturn(1L);
        when(jwtService.getRole("refresh_token")).thenReturn(Role.ADMIN);
        when(jwtService.generateAccessToken(1L, Role.ADMIN)).thenReturn("new_access");
        when(jwtService.generateRefreshToken(1L, Role.ADMIN)).thenReturn("new_refresh");

        AuthResponse response = authService.refresh("refresh_token");

        assertThat(response.getAccessToken()).isEqualTo("new_access");
    }

    @Test
    void refresh_ShouldThrow_WhenTokenInvalid() {
        when(jwtService.isTokenValid("bad_token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh("bad_token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenValid() {
        when(jwtService.isTokenValid("valid_token")).thenReturn(true);
        assertThat(authService.validateToken("valid_token")).isTrue();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenInvalid() {
        when(jwtService.isTokenValid("bad_token")).thenReturn(false);
        assertThat(authService.validateToken("bad_token")).isFalse();
    }
}
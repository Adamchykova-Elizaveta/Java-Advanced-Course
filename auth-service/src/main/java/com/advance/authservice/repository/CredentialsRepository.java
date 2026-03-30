package com.advance.authservice.repository;

import com.advance.authservice.entity.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredentialsRepository extends JpaRepository<Credentials, Long> {
    Optional<Credentials> findByLogin(String login);
    boolean existsByLogin(String login);
    boolean existsByUserId(Long userId);
}
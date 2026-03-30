package com.advance.auth.repository;

import com.advance.auth.entity.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredentialsRepository extends JpaRepository<Credentials, Long> {
    Optional<Credentials> findByLogin(String login);
    boolean existsByLogin(String login);
    boolean existsByUserId(Long userId);
}
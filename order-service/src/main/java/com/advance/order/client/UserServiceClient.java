package com.advance.order.client;

import com.advance.order.dto.UserDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${user-service.url}")
    private String userServiceUrl;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    public UserDto getUserById(Long userId) {
        return restTemplate.getForObject(
                userServiceUrl + "/api/users/" + userId,
                UserDto.class
        );
    }

    public UserDto getUserFallback(Long userId, Exception ex) {
        log.warn("User service unavailable for userId: {}. Error: {}", userId, ex.getMessage());
        return UserDto.builder()
                .id(userId)
                .name("Unknown")
                .surname("Unknown")
                .email("unavailable")
                .build();
    }
}
package com.advance.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AuthServiceApplication.class);
        app.setAdditionalProfiles("local");
        app.run(args);
    }
}
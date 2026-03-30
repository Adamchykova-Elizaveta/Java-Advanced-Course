package com.advance.auth.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank
    private String login;
    @NotBlank
    private String password;
}

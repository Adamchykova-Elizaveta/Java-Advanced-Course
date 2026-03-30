package com.advance.auth.dto;

import com.advance.auth.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CredentialsDto {

    @NotBlank
    private String login;
    @NotBlank
    private String password;
    @NotNull
    private Long userId;
    @NotNull
    private Role role;
}
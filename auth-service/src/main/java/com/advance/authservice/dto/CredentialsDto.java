package com.advance.authservice.dto;

import com.advance.authservice.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

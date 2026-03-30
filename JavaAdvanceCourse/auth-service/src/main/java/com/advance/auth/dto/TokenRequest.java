package com.advance.auth.dto;
import lombok.*;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenRequest {

    @NotBlank
    private String token;
}
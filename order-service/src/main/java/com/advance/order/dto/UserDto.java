package com.advance.order.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserDto {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private LocalDate birthDate;
    private Boolean active;
}
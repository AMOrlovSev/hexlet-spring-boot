package io.hexlet.spring_boot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserCreateDTO {
    @NotBlank
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
}

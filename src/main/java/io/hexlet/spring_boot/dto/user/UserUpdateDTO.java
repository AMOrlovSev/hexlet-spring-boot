package io.hexlet.spring_boot.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.LocalDate;

@Getter
@Setter
public class UserUpdateDTO {
    @NotNull private JsonNullable<String> email = JsonNullable.undefined();
    @NotNull private JsonNullable<String> firstName = JsonNullable.undefined();
    @NotNull private JsonNullable<String> lastName = JsonNullable.undefined();
    @NotNull private JsonNullable<LocalDate> birthday = JsonNullable.undefined();
}
package io.hexlet.spring_boot.dto.tag;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class TagUpdateDTO {

    @NotNull private JsonNullable<String> name = JsonNullable.undefined();
}
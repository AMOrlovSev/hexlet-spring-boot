package io.hexlet.spring_boot.dto.post;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;

@Getter
@Setter
public class PostUpdateDTO {
    @NotNull private JsonNullable<String> title = JsonNullable.undefined();
    @NotNull private JsonNullable<String> content = JsonNullable.undefined();
    @NotNull private JsonNullable<Boolean> published = JsonNullable.undefined();
    @NotNull private JsonNullable<Long> authorId = JsonNullable.undefined();
    @NotNull private JsonNullable<List<Long>> tagIds = JsonNullable.undefined();
}
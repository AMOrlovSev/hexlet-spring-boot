package io.hexlet.spring_boot.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostCreateDTO {
    @NotBlank @Size(min = 3, max = 200)
    private String title;

    @NotBlank @Size(min = 3)
    private String content;

    private Boolean published;

    private Long authorId;

    private List<Long> tagIds;
}
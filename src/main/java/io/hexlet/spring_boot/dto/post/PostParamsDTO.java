package io.hexlet.spring_boot.dto.post;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PostParamsDTO {
    private String nameCont;
    private Long authorId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAtGt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAtLt;

    private Boolean published;

    private Long tagId;
    private List<Long> tagsIn;
}
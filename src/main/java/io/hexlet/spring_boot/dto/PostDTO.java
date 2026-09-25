package io.hexlet.spring_boot.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostDTO {
    private Long id;
    private String title;
    private String content;
    private boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

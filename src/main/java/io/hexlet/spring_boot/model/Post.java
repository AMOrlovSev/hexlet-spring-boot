package io.hexlet.spring_boot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class Post {
    private String title;
    private String content;
    private String author;
    private LocalDateTime createdAt;
}

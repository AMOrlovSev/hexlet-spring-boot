package io.hexlet.spring_boot.dto.tag;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagDTO {
    private Long id;
    private String name;
}
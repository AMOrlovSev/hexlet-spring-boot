package io.hexlet.spring_boot.mapper;

import io.hexlet.spring_boot.dto.PostCreateDTO;
import io.hexlet.spring_boot.dto.PostDTO;
import io.hexlet.spring_boot.dto.PostUpdateDTO;
import io.hexlet.spring_boot.model.Post;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PostMapper {

    PostDTO toDTO(Post post);

    Post toEntity(PostCreateDTO dto);

    void updateEntityFromDTO(PostUpdateDTO dto, @MappingTarget Post post);
}
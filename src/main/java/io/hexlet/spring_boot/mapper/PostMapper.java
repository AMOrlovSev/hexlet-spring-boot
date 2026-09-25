package io.hexlet.spring_boot.mapper;

import io.hexlet.spring_boot.dto.PostCreateDTO;
import io.hexlet.spring_boot.dto.PostDTO;
import io.hexlet.spring_boot.dto.PostPatchDTO;
import io.hexlet.spring_boot.dto.PostUpdateDTO;
import io.hexlet.spring_boot.model.Post;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        uses = {JsonNullableMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PostMapper {

    public abstract PostDTO toDTO(Post post);

    public abstract Post toEntity(PostCreateDTO dto);

    public abstract void updateEntityFromDTO(PostUpdateDTO dto, @MappingTarget Post post);

    public abstract void updateEntityFromDTO(PostPatchDTO dto, @MappingTarget Post post);
}
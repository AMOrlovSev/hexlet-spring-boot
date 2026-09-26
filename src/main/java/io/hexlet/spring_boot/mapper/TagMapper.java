package io.hexlet.spring_boot.mapper;

import io.hexlet.spring_boot.dto.tag.TagCreateDTO;
import io.hexlet.spring_boot.dto.tag.TagDTO;
import io.hexlet.spring_boot.dto.tag.TagUpdateDTO;
import io.hexlet.spring_boot.model.Tag;
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
public abstract class TagMapper {

    public abstract TagDTO toDTO(Tag tag);

    public abstract Tag toEntity(TagCreateDTO dto);

    public abstract void update(TagUpdateDTO dto, @MappingTarget Tag tag);
}
package io.hexlet.spring_boot.mapper;

import io.hexlet.spring_boot.dto.post.PostCreateDTO;
import io.hexlet.spring_boot.dto.post.PostDTO;
import io.hexlet.spring_boot.dto.post.PostUpdateDTO;
import io.hexlet.spring_boot.model.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class, TagMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PostMapper {

    @Mapping(source = "author.id", target = "authorId")
    public abstract PostDTO toDTO(Post post);

    // authorId (Long) → author (User) через ReferenceMapper
    // tags — не мапим, обрабатываем вручную в контроллере
    @Mapping(target = "author", source = "authorId")
    @Mapping(target = "tags", ignore = true)
    public abstract Post toEntity(PostCreateDTO dto);

    // authorId (JsonNullable<Long>) → author (User)
    // JsonNullableMapper.unwrap() снимает обёртку, ReferenceMapper.toEntity() находит сущность
    @Mapping(target = "author", source = "authorId")
    @Mapping(target = "tags", ignore = true)
    public abstract void updateEntityFromDTO(PostUpdateDTO dto, @MappingTarget Post post);
}
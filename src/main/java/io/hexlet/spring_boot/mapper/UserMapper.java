package io.hexlet.spring_boot.mapper;

import io.hexlet.spring_boot.dto.PostCreateDTO;
import io.hexlet.spring_boot.dto.PostDTO;
import io.hexlet.spring_boot.dto.PostUpdateDTO;
import io.hexlet.spring_boot.dto.UserCreateDTO;
import io.hexlet.spring_boot.dto.UserDTO;
import io.hexlet.spring_boot.dto.UserPatchDTO;
import io.hexlet.spring_boot.dto.UserUpdateDTO;
import io.hexlet.spring_boot.model.Post;
import io.hexlet.spring_boot.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

@Mapper(
        // Подключение JsonNullableMapper
        uses = {JsonNullableMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserMapper {

    public abstract UserDTO toDTO(User user);

    public abstract User toEntity(UserCreateDTO dto);

    public abstract void updateEntityFromDTO(UserUpdateDTO dto, @MappingTarget User user);

    public abstract void updateEntityFromDTO(UserPatchDTO dto, @MappingTarget User user);
}

package io.hexlet.spring_boot.mapper;

import io.hexlet.spring_boot.dto.user.UserCreateDTO;
import io.hexlet.spring_boot.dto.user.UserDTO;
import io.hexlet.spring_boot.dto.user.UserUpdateDTO;
import io.hexlet.spring_boot.model.User;
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
public abstract class UserMapper {

    public abstract UserDTO toDTO(User user);

    public abstract User toEntity(UserCreateDTO dto);

    public abstract void updateEntityFromDTO(UserUpdateDTO dto, @MappingTarget User user);
}
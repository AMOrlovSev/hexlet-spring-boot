package io.hexlet.spring_boot.mapper;

import io.hexlet.spring_boot.dto.PostCreateDTO;
import io.hexlet.spring_boot.dto.PostDTO;
import io.hexlet.spring_boot.dto.PostUpdateDTO;
import io.hexlet.spring_boot.dto.UserCreateDTO;
import io.hexlet.spring_boot.dto.UserDTO;
import io.hexlet.spring_boot.dto.UserUpdateDTO;
import io.hexlet.spring_boot.model.Post;
import io.hexlet.spring_boot.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDTO(User user);

    User toEntity(UserCreateDTO dto);

    void updateEntityFromDTO(UserUpdateDTO dto, @MappingTarget User user);
}

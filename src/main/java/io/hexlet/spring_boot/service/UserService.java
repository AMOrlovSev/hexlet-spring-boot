package io.hexlet.spring_boot.service;

import io.hexlet.spring_boot.dto.user.UserCreateDTO;
import io.hexlet.spring_boot.dto.user.UserDTO;
import io.hexlet.spring_boot.dto.user.UserUpdateDTO;
import io.hexlet.spring_boot.exception.ResourceNotFoundException;
import io.hexlet.spring_boot.mapper.UserMapper;
import io.hexlet.spring_boot.model.User;
import io.hexlet.spring_boot.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public Page<UserDTO> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDTO);
    }

    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
        return userMapper.toDTO(user);
    }

    public UserDTO create(UserCreateDTO dto) {
        User user = userMapper.toEntity(dto);
        userRepository.save(user);
        return userMapper.toDTO(user);
    }

    public UserDTO update(Long id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));

        userMapper.updateEntityFromDTO(dto, user);
        userRepository.save(user);
        return userMapper.toDTO(user);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
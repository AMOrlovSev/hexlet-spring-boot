package io.hexlet.spring_boot.controller;

import io.hexlet.spring_boot.dto.UserDTO;
import io.hexlet.spring_boot.exception.ResourceNotFoundException;
import io.hexlet.spring_boot.mapper.UserMapper;
import io.hexlet.spring_boot.model.User;
import io.hexlet.spring_boot.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserController(UserRepository userRepository, UserMapper userMapper) {
         this.userRepository = userRepository;
         this.userMapper = userMapper;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> index(@RequestParam(defaultValue = "10") Integer limit) {
        List<User> users = userRepository.findAll();
        List<UserDTO> usersDTO = users.stream().limit(limit).map(userMapper::toDTO).toList();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(users.size()))
                .body(usersDTO);
    }

    @PostMapping
    public ResponseEntity<UserDTO> create(@Valid @RequestBody User data) {
        User user = new User();
        user.setEmail(data.getEmail());
        user.setFirstName(data.getFirstName());
        user.setLastName(data.getLastName());
        user.setBirthday(data.getBirthday());

        User saved = userRepository.save(user);
        UserDTO userDTO = userMapper.toDTO(saved);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(userDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> show(@PathVariable Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return ResponseEntity.ok(userMapper.toDTO(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable Long id, @Valid @RequestBody User data) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setEmail(data.getEmail());
        user.setFirstName(data.getFirstName());
        user.setLastName(data.getLastName());
        user.setBirthday(data.getBirthday());

        User updated = userRepository.save(user);

        return ResponseEntity.ok(userMapper.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

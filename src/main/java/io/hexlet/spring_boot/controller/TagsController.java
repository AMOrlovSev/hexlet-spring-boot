package io.hexlet.spring_boot.controller;

import io.hexlet.spring_boot.dto.tag.TagCreateDTO;
import io.hexlet.spring_boot.dto.tag.TagDTO;
import io.hexlet.spring_boot.dto.tag.TagUpdateDTO;
import io.hexlet.spring_boot.exception.ResourceNotFoundException;
import io.hexlet.spring_boot.mapper.TagMapper;
import io.hexlet.spring_boot.model.Tag;
import io.hexlet.spring_boot.repository.TagRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagsController {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public TagsController(TagRepository tagRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    @GetMapping
    public ResponseEntity<List<TagDTO>> index(
            @RequestParam(required = false) String name) {

        List<Tag> tags = (name == null || name.isBlank())
                ? tagRepository.findAll()
                : tagRepository.findByNameContainingIgnoreCase(name);

        List<TagDTO> dto = tags.stream().map(tagMapper::toDTO).toList();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(dto.size()))
                .body(dto);
    }

    @PostMapping
    public ResponseEntity<TagDTO> create(@Valid @RequestBody TagCreateDTO dto) {
        tagRepository.findByName(dto.getName()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Tag with name '" + dto.getName() + "' already exists");
        });

        Tag tag = tagMapper.toEntity(dto);
        tagRepository.save(tag);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(tag.getId())
                .toUri();

        return ResponseEntity.created(location).body(tagMapper.toDTO(tag));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> show(@PathVariable Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tag not found with id: " + id));
        return ResponseEntity.ok(tagMapper.toDTO(tag));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TagDTO> update(@PathVariable Long id,
                                         @Valid @RequestBody TagUpdateDTO dto) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tag not found with id: " + id));

        if (dto.getName() != null && dto.getName().isPresent()) {
            String newName = dto.getName().get();
            tagRepository.findByName(newName).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Tag with name '" + newName + "' already exists");
                }
            });
        }

        tagMapper.update(dto, tag);
        tagRepository.save(tag);
        return ResponseEntity.ok(tagMapper.toDTO(tag));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        if (!tagRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tag not found with id: " + id);
        }
        tagRepository.removeTagFromPosts(id);
        tagRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
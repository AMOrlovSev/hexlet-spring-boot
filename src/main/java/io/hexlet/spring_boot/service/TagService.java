package io.hexlet.spring_boot.service;

import io.hexlet.spring_boot.dto.tag.TagCreateDTO;
import io.hexlet.spring_boot.dto.tag.TagDTO;
import io.hexlet.spring_boot.dto.tag.TagUpdateDTO;
import io.hexlet.spring_boot.exception.ResourceNotFoundException;
import io.hexlet.spring_boot.mapper.TagMapper;
import io.hexlet.spring_boot.model.Tag;
import io.hexlet.spring_boot.repository.TagRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public TagService(TagRepository tagRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    public List<TagDTO> getAll(String name) {
        List<Tag> tags = (name == null || name.isBlank())
                ? tagRepository.findAll()
                : tagRepository.findByNameContainingIgnoreCase(name);
        return tags.stream().map(tagMapper::toDTO).toList();
    }

    public TagDTO findById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tag not found with id: " + id));
        return tagMapper.toDTO(tag);
    }

    public TagDTO create(TagCreateDTO dto) {
        tagRepository.findByName(dto.getName()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Tag with name '" + dto.getName() + "' already exists");
        });

        Tag tag = tagMapper.toEntity(dto);
        tagRepository.save(tag);
        return tagMapper.toDTO(tag);
    }

    public TagDTO update(Long id, TagUpdateDTO dto) {
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
        return tagMapper.toDTO(tag);
    }

    @Transactional
    public void delete(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tag not found with id: " + id);
        }
        tagRepository.removeTagFromPosts(id);
        tagRepository.deleteById(id);
    }
}
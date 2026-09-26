package io.hexlet.spring_boot.controller;

import io.hexlet.spring_boot.dto.tag.TagCreateDTO;
import io.hexlet.spring_boot.dto.tag.TagDTO;
import io.hexlet.spring_boot.dto.tag.TagUpdateDTO;
import io.hexlet.spring_boot.service.TagService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagsController {

    private final TagService tagService;

    public TagsController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<List<TagDTO>> index(
            @RequestParam(required = false) String name) {

        List<TagDTO> dto = tagService.getAll(name);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(dto.size()))
                .body(dto);
    }

    @PostMapping
    public ResponseEntity<TagDTO> create(@Valid @RequestBody TagCreateDTO dto) {
        TagDTO created = tagService.create(dto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> show(@PathVariable Long id) {
        return ResponseEntity.ok(tagService.findById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TagDTO> update(@PathVariable Long id,
                                         @Valid @RequestBody TagUpdateDTO dto) {
        return ResponseEntity.ok(tagService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
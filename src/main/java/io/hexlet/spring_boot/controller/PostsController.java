package io.hexlet.spring_boot.controller;

import io.hexlet.spring_boot.dto.post.PostCreateDTO;
import io.hexlet.spring_boot.dto.post.PostDTO;
import io.hexlet.spring_boot.dto.post.PostParamsDTO;
import io.hexlet.spring_boot.dto.post.PostUpdateDTO;
import io.hexlet.spring_boot.service.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/posts")
public class PostsController {

    private final PostService postService;

    public PostsController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<Page<PostDTO>> index(
            PostParamsDTO params,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<PostDTO> dto = postService.getAll(params, pageable);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(dto.getTotalElements()))
                .body(dto);
    }

    @PostMapping
    public ResponseEntity<PostDTO> create(@Valid @RequestBody PostCreateDTO dto) {
        PostDTO created = postService.create(dto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> show(@PathVariable Long id) {
        return ResponseEntity.ok(postService.findById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PostDTO> update(@PathVariable Long id,
                                          @Valid @RequestBody PostUpdateDTO dto) {
        return ResponseEntity.ok(postService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
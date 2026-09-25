package io.hexlet.spring_boot.controller;

import io.hexlet.spring_boot.dto.PostCreateDTO;
import io.hexlet.spring_boot.dto.PostDTO;
import io.hexlet.spring_boot.dto.PostPatchDTO;
import io.hexlet.spring_boot.dto.PostUpdateDTO;
import io.hexlet.spring_boot.dto.UserDTO;
import io.hexlet.spring_boot.dto.UserPatchDTO;
import io.hexlet.spring_boot.exception.ResourceNotFoundException;
import io.hexlet.spring_boot.mapper.PostMapper;
import io.hexlet.spring_boot.model.Post;
import io.hexlet.spring_boot.repository.PostRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class PostController {
    private final PostRepository postRepository;
    private final PostMapper postMapper;

    public PostController(PostRepository postRepository, PostMapper postMapper) {
        this.postRepository = postRepository;
        this.postMapper = postMapper;
    }

    @GetMapping("/posts")
    public ResponseEntity<Page<PostDTO>> index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Post> posts = postRepository.findByPublishedTrue(pageable);
        Page<PostDTO> postsDTO = posts.map(postMapper::toDTO);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(posts.getTotalElements()))
                .body(postsDTO);
    }

    @PostMapping("/posts")
    public ResponseEntity<PostDTO> create(@Valid @RequestBody PostCreateDTO data) {
        Post post = postMapper.toEntity(data);

        Post saved = postRepository.save(post);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(postMapper.toDTO(saved));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<PostDTO> show(@PathVariable Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
        return ResponseEntity.ok(postMapper.toDTO(post));
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<PostDTO> update(@PathVariable Long id, @Valid @RequestBody PostUpdateDTO dto) {
        Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        postMapper.updateEntityFromDTO(dto, post);
        postRepository.save(post);
        PostDTO postDTO = postMapper.toDTO(post);

        return ResponseEntity.ok(postDTO);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        if (!postRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        postRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/posts/{id}")
    public ResponseEntity<PostDTO> patch(@PathVariable Long id, @RequestBody PostPatchDTO dto) {
        var post =
                postRepository
                        .findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        postMapper.updateEntityFromDTO(dto, post);

        postRepository.save(post);
        return ResponseEntity.ok(postMapper.toDTO(post));
    }
}

package io.hexlet.spring_boot.controller;

import io.hexlet.spring_boot.dto.post.PostCreateDTO;
import io.hexlet.spring_boot.dto.post.PostDTO;
import io.hexlet.spring_boot.dto.post.PostUpdateDTO;
import io.hexlet.spring_boot.exception.ResourceNotFoundException;
import io.hexlet.spring_boot.mapper.PostMapper;
import io.hexlet.spring_boot.model.Post;
import io.hexlet.spring_boot.model.Tag;
import io.hexlet.spring_boot.repository.PostRepository;
import io.hexlet.spring_boot.repository.TagRepository;
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
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostsController {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostMapper postMapper;

    public PostsController(PostRepository postRepository,
                           TagRepository tagRepository,
                           PostMapper postMapper) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.postMapper = postMapper;
    }

    @GetMapping
    public ResponseEntity<Page<PostDTO>> index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Post> posts = postRepository.findByPublishedTrue(pageable);
        Page<PostDTO> dto = posts.map(postMapper::toDTO);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(posts.getTotalElements()))
                .body(dto);
    }

    @PostMapping
    public ResponseEntity<PostDTO> create(@Valid @RequestBody PostCreateDTO dto) {
        Post post = postMapper.toEntity(dto);
        attachTags(post, dto.getTagIds());
        postRepository.save(post);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(post.getId())
                .toUri();

        return ResponseEntity.created(location).body(postMapper.toDTO(post));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> show(@PathVariable Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + id));
        return ResponseEntity.ok(postMapper.toDTO(post));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PostDTO> update(@PathVariable Long id,
                                          @Valid @RequestBody PostUpdateDTO dto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + id));

        postMapper.updateEntityFromDTO(dto, post);

        if (dto.getTagIds() != null && dto.getTagIds().isPresent()) {
            replaceTags(post, dto.getTagIds().get());
        }

        postRepository.save(post);
        return ResponseEntity.ok(postMapper.toDTO(post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post not found with id: " + id);
        }
        postRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== helpers ====================

    private void attachTags(Post post, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return;

        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Some tags not found: " + tagIds);
        }
        tags.forEach(post::addTag);
    }

    private void replaceTags(Post post, List<Long> newTagIds) {
        post.getTags().forEach(tag -> tag.getPosts().remove(post));
        post.getTags().clear();
        attachTags(post, newTagIds);
    }
}
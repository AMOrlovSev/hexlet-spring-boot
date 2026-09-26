package io.hexlet.spring_boot.service;

import io.hexlet.spring_boot.dto.post.PostCreateDTO;
import io.hexlet.spring_boot.dto.post.PostDTO;
import io.hexlet.spring_boot.dto.post.PostParamsDTO;
import io.hexlet.spring_boot.dto.post.PostUpdateDTO;
import io.hexlet.spring_boot.exception.ResourceNotFoundException;
import io.hexlet.spring_boot.mapper.PostMapper;
import io.hexlet.spring_boot.model.Post;
import io.hexlet.spring_boot.model.Tag;
import io.hexlet.spring_boot.repository.PostRepository;
import io.hexlet.spring_boot.repository.TagRepository;
import io.hexlet.spring_boot.specification.PostSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostMapper postMapper;
    private final PostSpecification postSpecification;

    public PostService(PostRepository postRepository,
                       TagRepository tagRepository,
                       PostMapper postMapper,
                       PostSpecification postSpecification) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.postMapper = postMapper;
        this.postSpecification = postSpecification;
    }

    public Page<PostDTO> getAll(PostParamsDTO params, Pageable pageable) {
        Specification<Post> spec = postSpecification.build(params);
        return postRepository.findAll(spec, pageable).map(postMapper::toDTO);
    }

    public PostDTO findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + id));
        return postMapper.toDTO(post);
    }

    public PostDTO create(PostCreateDTO dto) {
        Post post = postMapper.toEntity(dto);
        attachTags(post, dto.getTagIds());
        postRepository.save(post);
        return postMapper.toDTO(post);
    }

    public PostDTO update(Long id, PostUpdateDTO dto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + id));

        postMapper.updateEntityFromDTO(dto, post);

        if (dto.getTagIds() != null && dto.getTagIds().isPresent()) {
            replaceTags(post, dto.getTagIds().get());
        }

        postRepository.save(post);
        return postMapper.toDTO(post);
    }

    public void delete(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post not found with id: " + id);
        }
        postRepository.deleteById(id);
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
package io.hexlet.spring_boot.mapper;

import io.hexlet.spring_boot.dto.PostCreateDTO;
import io.hexlet.spring_boot.dto.PostDTO;
import io.hexlet.spring_boot.model.Post;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    public PostDTO toDTO(Post post) {
        PostDTO postDTO = new PostDTO();
        postDTO.setId(post.getId());
        postDTO.setTitle(post.getTitle());
        postDTO.setContent(post.getContent());
        postDTO.setPublished(post.isPublished());
        postDTO.setCreatedAt(post.getCreatedAt());
        postDTO.setUpdatedAt(post.getUpdatedAt());
        return postDTO;
    }

    public Post toEntity(PostCreateDTO dto) {
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        return post;
    }
}

package io.hexlet.spring_boot.repository;

import io.hexlet.spring_boot.model.Post;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    @Override
    @EntityGraph(attributePaths = {"author", "tags"})
    Page<Post> findAll(@Nullable Specification<Post> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "tags"})
    Page<Post> findByPublishedTrue(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"author", "tags"})
    Optional<Post> findById(Long id);
}

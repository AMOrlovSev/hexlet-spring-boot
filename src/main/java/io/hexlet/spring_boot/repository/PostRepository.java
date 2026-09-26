package io.hexlet.spring_boot.repository;

import io.hexlet.spring_boot.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = {"author", "tags"})
    Page<Post> findByPublishedTrue(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"author", "tags"})
    Optional<Post> findById(Long id);
}

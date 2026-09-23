package io.hexlet.spring_boot.repository;

import io.hexlet.spring_boot.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {}
package io.hexlet.spring_boot.repository;

import io.hexlet.spring_boot.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    List<Tag> findByNameContainingIgnoreCase(String name);

    @Modifying
    @Query(value = "DELETE FROM post_tags WHERE tag_id = :tagId", nativeQuery = true)
    void removeTagFromPosts(@Param("tagId") Long tagId);
}
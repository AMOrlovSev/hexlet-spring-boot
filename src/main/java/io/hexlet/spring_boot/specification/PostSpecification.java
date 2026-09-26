package io.hexlet.spring_boot.specification;

import io.hexlet.spring_boot.dto.post.PostParamsDTO;
import io.hexlet.spring_boot.model.Post;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PostSpecification {

    public Specification<Post> build(PostParamsDTO params) {
        Specification<Post> spec = (root, query, cb) -> cb.conjunction();

        spec = spec.and(withAuthorId(params.getAuthorId()));
        spec = spec.and(withNameCont(params.getNameCont()));
        spec = spec.and(withCreatedAtGt(params.getCreatedAtGt()));
        spec = spec.and(withCreatedAtLt(params.getCreatedAtLt()));
        spec = spec.and(withTagId(params.getTagId()));
        spec = spec.and(withTagsIn(params.getTagsIn()));
        spec = spec.and(withPublished(params.getPublished()));
        spec = spec.and(distinct());

        return spec;
    }

    private Specification<Post> withAuthorId(Long authorId) {
        return (root, query, cb) ->
                authorId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("author").get("id"), authorId);
    }

    private Specification<Post> withNameCont(String nameCont) {
        return (root, query, cb) -> {
            if (nameCont == null || nameCont.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.lower(root.get("title")),
                    "%" + nameCont.toLowerCase() + "%"
            );
        };
    }

    private Specification<Post> withCreatedAtGt(LocalDateTime date) {
        return (root, query, cb) ->
                date == null
                        ? cb.conjunction()
                        : cb.greaterThan(root.get("createdAt"), date);
    }

    private Specification<Post> withCreatedAtLt(LocalDateTime date) {
        return (root, query, cb) ->
                date == null
                        ? cb.conjunction()
                        : cb.lessThan(root.get("createdAt"), date);
    }

    private Specification<Post> withTagId(Long tagId) {
        return (root, query, cb) ->
                tagId == null
                        ? cb.conjunction()
                        : cb.equal(root.join("tags").get("id"), tagId);
    }

    private Specification<Post> withTagsIn(List<Long> tagIds) {
        return (root, query, cb) -> {
            if (tagIds == null || tagIds.isEmpty()) {
                return cb.conjunction();
            }
            return root.join("tags").get("id").in(tagIds);
        };
    }

    private Specification<Post> withPublished(Boolean published) {
        return (root, query, cb) ->
                published == null
                        ? cb.conjunction()
                        : cb.equal(root.get("published"), published);
    }

    private Specification<Post> distinct() {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.conjunction();
        };
    }

}
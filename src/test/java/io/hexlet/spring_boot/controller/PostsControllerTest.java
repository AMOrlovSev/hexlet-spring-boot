package io.hexlet.spring_boot.controller;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.hexlet.spring_boot.dto.post.PostCreateDTO;
import io.hexlet.spring_boot.dto.post.PostUpdateDTO;
import io.hexlet.spring_boot.model.Post;
import io.hexlet.spring_boot.model.Tag;
import io.hexlet.spring_boot.model.User;
import io.hexlet.spring_boot.repository.PostRepository;
import io.hexlet.spring_boot.repository.TagRepository;
import io.hexlet.spring_boot.repository.UserRepository;

import org.instancio.Instancio;
import org.instancio.Select;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class PostsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper om;
    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TagRepository tagRepository;

    private User author;
    private Tag springTag;
    private Post publishedPost;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        tagRepository.deleteAll();
        userRepository.deleteAll();

        author = userRepository.save(newUser("author@example.com"));
        springTag = tagRepository.save(newTag("spring"));

        publishedPost = postRepository.save(
                newPost("Hello World", "Published content body", true, author, List.of(springTag)));
    }

    // ==================== INDEX ====================

    @Test
    void testIndexReturnsOnlyPublishedPostsWithFilter() throws Exception {
        postRepository.save(newPost("Draft post", "Draft content", false, author, List.of()));

        mockMvc.perform(get("/api/posts").param("published", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Hello World"))
                .andExpect(jsonPath("$.content[0].published").value(true));
    }

    @Test
    void testIndexIncludesXTotalCountHeader() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(header().string("X-Total-Count", "1"));
    }

    // ==================== SHOW ====================

    @Test
    void testShowReturnsPostWithTagsAndAuthor() throws Exception {
        var result = mockMvc.perform(get("/api/posts/" + publishedPost.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(publishedPost.getId()))
                .andExpect(jsonPath("$.title").value("Hello World"))
                .andExpect(jsonPath("$.content").value("Published content body"))
                .andExpect(jsonPath("$.published").value(true))
                .andExpect(jsonPath("$.authorId").value(author.getId()))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags.length()").value(1))
                .andReturn();

        assertThatJson(result.getResponse().getContentAsString())
                .and(
                        v -> v.node("id").isEqualTo(publishedPost.getId()),
                        v -> v.node("title").isEqualTo("Hello World"),
                        v -> v.node("content").isEqualTo("Published content body"),
                        v -> v.node("published").isEqualTo(true),
                        v -> v.node("authorId").isEqualTo(author.getId())
                );
    }

    @Test
    void testShowReturns404ForUnknownPost() throws Exception {
        mockMvc.perform(get("/api/posts/99999"))
                .andExpect(status().isNotFound());
    }

    // ==================== CREATE ====================

    @Test
    void testCreatePostWithTagAndAuthor() throws Exception {
        var dto = Instancio.of(PostCreateDTO.class)
                .set(Select.field(PostCreateDTO::getAuthorId), author.getId())
                .set(Select.field(PostCreateDTO::getTagIds), List.of(springTag.getId()))
                .create();

        var result = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(dto.getTitle()))
                .andExpect(jsonPath("$.authorId").value(author.getId()))
                .andExpect(jsonPath("$.tags", hasSize(1)))
                .andExpect(jsonPath("$.tags[0].name").value("spring"))
                .andReturn();

        Long createdId = om.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();

        var created = postRepository.findById(createdId).orElseThrow();

        assertThat(created.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(created.getTags()).hasSize(1);
        assertThat(created.getTags().get(0).getName()).isEqualTo("spring");
    }

    @Test
    void testCreatePostWithoutTags() throws Exception {
        var dto = Instancio.of(PostCreateDTO.class)
                .set(Select.field(PostCreateDTO::getAuthorId), author.getId())
                .set(Select.field(PostCreateDTO::getTagIds), null)
                .create();

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tags").isEmpty());
    }

    @Test
    void testCreatePostWithUnknownTagReturns400() throws Exception {
        var dto = Instancio.of(PostCreateDTO.class)
                .set(Select.field(PostCreateDTO::getAuthorId), author.getId())
                .set(Select.field(PostCreateDTO::getTagIds), List.of(99999L))
                .create();

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreatePostWithInvalidTitleReturns422() throws Exception {
        var dto = Instancio.of(PostCreateDTO.class)
                .set(Select.field(PostCreateDTO::getTitle), "ab") // меньше min=3
                .set(Select.field(PostCreateDTO::getAuthorId), author.getId())
                .create();

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").exists());
    }

    // ==================== PATCH ====================

    @Test
    void testPatchTitle() throws Exception {
        var dto = new PostUpdateDTO();
        dto.setTitle(JsonNullable.of("Patched title"));

        mockMvc.perform(patch("/api/posts/" + publishedPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Patched title"))
                .andExpect(jsonPath("$.content").value("Published content body")); // не тронуто
    }

    @Test
    void testPatchReplaceTags() throws Exception {
        var kotlinTag = tagRepository.save(newTag("kotlin"));

        var dto = new PostUpdateDTO();
        dto.setTagIds(JsonNullable.of(List.of(kotlinTag.getId())));

        mockMvc.perform(patch("/api/posts/" + publishedPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", hasSize(1)))
                .andExpect(jsonPath("$.tags[0].name").value("kotlin"));

        var reloadedPost = postRepository.findById(publishedPost.getId()).orElseThrow();
        assertThat(reloadedPost.getTags())
                .extracting(Tag::getName)
                .containsExactly("kotlin");
    }

    @Test
    void testPatchAuthor() throws Exception {
        var newAuthor = userRepository.save(newUser("new@example.com"));

        var dto = new PostUpdateDTO();
        dto.setAuthorId(JsonNullable.of(newAuthor.getId()));

        mockMvc.perform(patch("/api/posts/" + publishedPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorId").value(newAuthor.getId()));
    }

    @Test
    void testPatchReturns404ForUnknownPost() throws Exception {
        var dto = new PostUpdateDTO();
        dto.setTitle(JsonNullable.of("X"));

        mockMvc.perform(patch("/api/posts/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE ====================

    @Test
    void testDeletePost() throws Exception {
        mockMvc.perform(delete("/api/posts/" + publishedPost.getId()))
                .andExpect(status().isNoContent());

        assertThat(postRepository.existsById(publishedPost.getId())).isFalse();
    }

    @Test
    void testDeleteReturns404ForUnknownPost() throws Exception {
        mockMvc.perform(delete("/api/posts/99999"))
                .andExpect(status().isNotFound());
    }

    // ==================== FACTORIES (Instancio) ====================

    private User newUser(String email) {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .ignore(Select.field(User::getPosts))
                .set(Select.field(User::getEmail), email)
                .create();
    }

    private Tag newTag(String name) {
        return Instancio.of(Tag.class)
                .ignore(Select.field(Tag::getId))
                .ignore(Select.field(Tag::getPosts))
                .set(Select.field(Tag::getName), name)
                .create();
    }

    private Post newPost(String title, String content, boolean published,
                         User author, List<Tag> tags) {
        var post = Instancio.of(Post.class)
                .ignore(Select.field(Post::getId))
                .ignore(Select.field(Post::getCreatedAt))
                .ignore(Select.field(Post::getUpdatedAt))
                .ignore(Select.field(Post::getTags))
                .set(Select.field(Post::getTitle), title)
                .set(Select.field(Post::getContent), content)
                .set(Select.field(Post::isPublished), published)
                .set(Select.field(Post::getAuthor), author)
                .create();

        tags.forEach(post::addTag);
        return post;
    }
}
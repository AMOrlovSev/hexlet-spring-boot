package io.hexlet.spring_boot.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.hexlet.spring_boot.dto.tag.TagCreateDTO;
import io.hexlet.spring_boot.dto.tag.TagUpdateDTO;
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
class TagsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper om;
    @Autowired private TagRepository tagRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        tagRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ==================== INDEX ====================

    @Test
    void testIndexEmpty() throws Exception {
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty())
                .andExpect(header().string("X-Total-Count", "0"));
    }

    @Test
    void testIndexReturnsAllTags() throws Exception {
        saveTag("spring");
        saveTag("java");
        saveTag("kotlin");

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(header().string("X-Total-Count", "3"));
    }

    @Test
    void testIndexFiltersByNameCaseInsensitive() throws Exception {
        saveTag("spring");
        saveTag("spring-boot");
        saveTag("java");

        mockMvc.perform(get("/api/tags").param("name", "SPR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("spring"))
                .andExpect(jsonPath("$[1].name").value("spring-boot"));
    }

    // ==================== SHOW ====================

    @Test
    void testShowReturnsTag() throws Exception {
        var tag = saveTag("spring");

        mockMvc.perform(get("/api/tags/" + tag.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tag.getId()))
                .andExpect(jsonPath("$.name").value("spring"));
    }

    @Test
    void testShowReturns404ForUnknownTag() throws Exception {
        mockMvc.perform(get("/api/tags/99999"))
                .andExpect(status().isNotFound());
    }

    // ==================== CREATE ====================

    @Test
    void testCreateTag() throws Exception {
        var dto = Instancio.of(TagCreateDTO.class)
                .set(Select.field(TagCreateDTO::getName), "kotlin")
                .create();

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("kotlin"));

        assertThat(tagRepository.findByName("kotlin")).isPresent();
    }

    @Test
    void testCreateDuplicateTagReturns409() throws Exception {
        saveTag("spring");

        var dto = Instancio.of(TagCreateDTO.class)
                .set(Select.field(TagCreateDTO::getName), "spring")
                .create();

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void testCreateTagWithBlankNameReturns422() throws Exception {
        var dto = Instancio.of(TagCreateDTO.class)
                .set(Select.field(TagCreateDTO::getName), "")
                .create();

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.name").exists());
    }

    // ==================== PATCH ====================

    @Test
    void testPatchTagName() throws Exception {
        var tag = saveTag("old");

        var dto = new TagUpdateDTO();
        dto.setName(JsonNullable.of("new"));

        mockMvc.perform(patch("/api/tags/" + tag.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("new"));

        assertThat(tagRepository.findByName("new")).isPresent();
        assertThat(tagRepository.findByName("old")).isEmpty();
    }

    @Test
    void testPatchTagToDuplicateNameReturns409() throws Exception {
        var tagA = saveTag("aaa");
        saveTag("bbb");

        var dto = new TagUpdateDTO();
        dto.setName(JsonNullable.of("bbb"));

        mockMvc.perform(patch("/api/tags/" + tagA.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void testPatchTagToSameNameIsOk() throws Exception {
        var tag = saveTag("same");

        var dto = new TagUpdateDTO();
        dto.setName(JsonNullable.of("same"));

        mockMvc.perform(patch("/api/tags/" + tag.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("same"));
    }

    @Test
    void testPatchReturns404ForUnknownTag() throws Exception {
        var dto = new TagUpdateDTO();
        dto.setName(JsonNullable.of("x"));

        mockMvc.perform(patch("/api/tags/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE ====================

    @Test
    void testDeleteTag() throws Exception {
        var tag = saveTag("to-delete");

        mockMvc.perform(delete("/api/tags/" + tag.getId()))
                .andExpect(status().isNoContent());

        assertThat(tagRepository.findById(tag.getId())).isEmpty();
    }

    @Test
    void testDeleteTagUnlinksFromPosts() throws Exception {
        var user = userRepository.save(newUser("u@example.com"));
        var tag = saveTag("linked");
        var secondTag = saveTag("keep");

        var post = newPost("Post with tags", "Some content", user, List.of(tag, secondTag));
        postRepository.save(post);

        mockMvc.perform(delete("/api/tags/" + tag.getId()))
                .andExpect(status().isNoContent());

        assertThat(tagRepository.findById(tag.getId())).isEmpty();

        var reloadedPost = postRepository.findById(post.getId()).orElseThrow();
        assertThat(reloadedPost.getTags())
                .extracting(Tag::getName)
                .containsExactly("keep");
    }

    @Test
    void testDeleteReturns404ForUnknownTag() throws Exception {
        mockMvc.perform(delete("/api/tags/99999"))
                .andExpect(status().isNotFound());
    }

    // ==================== FACTORIES (Instancio) ====================

    private Tag saveTag(String name) {
        return tagRepository.save(newTag(name));
    }

    private Tag newTag(String name) {
        return Instancio.of(Tag.class)
                .ignore(Select.field(Tag::getId))
                .ignore(Select.field(Tag::getPosts))
                .set(Select.field(Tag::getName), name)
                .create();
    }

    private User newUser(String email) {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .ignore(Select.field(User::getPosts))
                .set(Select.field(User::getEmail), email)
                .create();
    }

    private Post newPost(String title, String content, User author, List<Tag> tags) {
        var post = Instancio.of(Post.class)
                .ignore(Select.field(Post::getId))
                .ignore(Select.field(Post::getCreatedAt))
                .ignore(Select.field(Post::getUpdatedAt))
                .ignore(Select.field(Post::getTags))
                .set(Select.field(Post::getTitle), title)
                .set(Select.field(Post::getContent), content)
                .set(Select.field(Post::isPublished), true)
                .set(Select.field(Post::getAuthor), author)
                .create();
        tags.forEach(post::addTag);
        return post;
    }
}
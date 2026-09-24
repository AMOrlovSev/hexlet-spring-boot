package io.hexlet.spring_boot.controller;

import io.hexlet.spring_boot.model.Post;
import io.hexlet.spring_boot.repository.PostRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.instancio.Select.field;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper objectMapper;

    private Post existingPost;

    @BeforeEach
    void setUp() {
        existingPost = new Post();
        existingPost.setTitle(faker.book().title());
        existingPost.setContent(faker.lorem().paragraph());
        existingPost.setPublished(true);
        existingPost = postRepository.save(existingPost);
    }

    @Test
    void index_returns200_andOnlyPublishedPosts() throws Exception {
        Post unpublished = new Post();
        unpublished.setTitle(faker.book().title());
        unpublished.setContent(faker.lorem().paragraph());
        unpublished.setPublished(false);
        postRepository.save(unpublished);

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].published", everyItem(is(true))));
    }

    @Test
    void create_returns201_andBody_andLocationHeader() throws Exception {
        Post data = Instancio.of(Post.class)
                .ignore(field(Post::getId))
                .set(field(Post::getTitle), "New Post Title")
                .create();

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("New Post Title"))
                .andExpect(jsonPath("$.content").value(data.getContent()))
                .andExpect(jsonPath("$.published").value(data.isPublished()));
    }

    @Test
    void create_withBlankTitle_returns422() throws Exception {
        Post data = Instancio.of(Post.class)
                .ignore(field(Post::getId))
                .set(field(Post::getTitle), "")
                .create();

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void show_existingPost_returns200_andBody() throws Exception {
        mockMvc.perform(get("/api/posts/{id}", existingPost.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingPost.getId()))
                .andExpect(jsonPath("$.title").value(existingPost.getTitle()));
    }

    @Test
    void show_missingPost_returns404() throws Exception {
        long missingId = existingPost.getId() + 1_000_000L;

        mockMvc.perform(get("/api/posts/{id}", missingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_existingPost_returns200_andUpdatedBody() throws Exception {
        Post data = Instancio.of(Post.class)
                .ignore(field(Post::getId))
                .set(field(Post::getTitle), "Updated Title")
                .create();

        mockMvc.perform(put("/api/posts/{id}", existingPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingPost.getId()))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value(data.getContent()));
    }

    @Test
    void update_missingPost_returns404() throws Exception {
        long missingId = existingPost.getId() + 1_000_000L;
        Post data = Instancio.of(Post.class)
                .ignore(field(Post::getId))
                .create();

        mockMvc.perform(put("/api/posts/{id}", missingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_withBlankTitle_returns422() throws Exception {
        Post data = Instancio.of(Post.class)
                .ignore(field(Post::getId))
                .set(field(Post::getTitle), "")
                .create();

        mockMvc.perform(put("/api/posts/{id}", existingPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void destroy_existingPost_returns204_andRemovesPost() throws Exception {
        mockMvc.perform(delete("/api/posts/{id}", existingPost.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/{id}", existingPost.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void destroy_missingPost_returns404() throws Exception {
        long missingId = existingPost.getId() + 1_000_000L;

        mockMvc.perform(delete("/api/posts/{id}", missingId))
                .andExpect(status().isNotFound());
    }
}
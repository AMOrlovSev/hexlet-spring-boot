package io.hexlet.spring_boot.controller;

import io.hexlet.spring_boot.model.User;
import io.hexlet.spring_boot.repository.UserRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Faker faker;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setFirstName(faker.name().firstName());
        existingUser.setLastName(faker.name().lastName());
        existingUser.setEmail(faker.internet().emailAddress());
        existingUser.setBirthday(faker.timeAndDate().birthday(18, 65));
        existingUser = userRepository.save(existingUser);
    }

    @Test
    void index_returns200_andListWithTotalCountHeader() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void create_returns201_andBody_andLocationHeader() throws Exception {
        var body = Map.of(
                "firstName", "John",
                "lastName", "Doe",
                "email", "john@example.com",
                "birthday", "1990-01-01"
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void create_withBlankEmail_returns422() throws Exception {
        var body = Map.of(
                "firstName", "John",
                "lastName", "Doe",
                "email", ""
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void show_existingUser_returns200_andBody() throws Exception {
        mockMvc.perform(get("/api/users/{id}", existingUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingUser.getId()))
                .andExpect(jsonPath("$.email").value(existingUser.getEmail()));
    }

    @Test
    void show_missingUser_returns404() throws Exception {
        long missingId = existingUser.getId() + 1_000_000L;

        mockMvc.perform(get("/api/users/{id}", missingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_existingUser_returns200_andUpdatedBody() throws Exception {
        var body = new HashMap<String, Object>();
        body.put("firstName", "Updated");
        body.put("lastName", existingUser.getLastName());
        body.put("email", "updated@example.com");
        body.put("birthday", LocalDate.of(1985, 5, 5).toString());

        mockMvc.perform(put("/api/users/{id}", existingUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingUser.getId()))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void update_missingUser_returns404() throws Exception {
        long missingId = existingUser.getId() + 1_000_000L;
        var body = Map.of(
                "firstName", "Ghost",
                "lastName", "User",
                "email", "ghost@example.com"
        );

        mockMvc.perform(put("/api/users/{id}", missingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_withBlankEmail_returns422() throws Exception {
        var body = Map.of(
                "firstName", existingUser.getFirstName(),
                "lastName", existingUser.getLastName(),
                "email", ""
        );

        mockMvc.perform(put("/api/users/{id}", existingUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void destroy_existingUser_returns204_andRemovesUser() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", existingUser.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", existingUser.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void destroy_missingUser_returns404() throws Exception {
        long missingId = existingUser.getId() + 1_000_000L;

        mockMvc.perform(delete("/api/users/{id}", missingId))
                .andExpect(status().isNotFound());
    }
}
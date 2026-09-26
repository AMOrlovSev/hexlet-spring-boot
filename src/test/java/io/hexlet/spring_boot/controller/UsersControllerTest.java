package io.hexlet.spring_boot.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.hexlet.spring_boot.dto.user.UserCreateDTO;
import io.hexlet.spring_boot.dto.user.UserUpdateDTO;
import io.hexlet.spring_boot.model.User;
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

@SpringBootTest
@AutoConfigureMockMvc
class UsersControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper om;
    @Autowired private UserRepository userRepository;

    private User existing;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        existing = userRepository.save(newUser("existing@example.com"));
    }

    // ==================== INDEX ====================

    @Test
    void testIndexReturnsUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].email").value("existing@example.com"))
                .andExpect(header().string("X-Total-Count", "1"));
    }

    @Test
    void testIndexWithPagination() throws Exception {
        for (int i = 0; i < 5; i++) {
            userRepository.save(newUser("u" + i + "@example.com"));
        }

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(6));
    }

    // ==================== SHOW ====================

    @Test
    void testShowReturnsUser() throws Exception {
        mockMvc.perform(get("/api/users/" + existing.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existing.getId()))
                .andExpect(jsonPath("$.email").value("existing@example.com"))
                .andExpect(jsonPath("$.firstName").value(existing.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(existing.getLastName()));
    }

    @Test
    void testShowReturns404ForUnknownUser() throws Exception {
        mockMvc.perform(get("/api/users/99999"))
                .andExpect(status().isNotFound());
    }

    // ==================== CREATE ====================

    @Test
    void testCreateUser() throws Exception {
        var dto = Instancio.of(UserCreateDTO.class)
                .set(Select.field(UserCreateDTO::getEmail), "new@example.com")
                .create();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.firstName").value(dto.getFirstName()));

        assertThat(userRepository.findAll())
                .extracting(User::getEmail)
                .contains("new@example.com");
    }

    @Test
    void testCreateUserWithoutBirthday() throws Exception {
        var dto = Instancio.of(UserCreateDTO.class)
                .set(Select.field(UserCreateDTO::getEmail), "nb@example.com")
                .set(Select.field(UserCreateDTO::getBirthday), null)
                .create();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("nb@example.com"));
    }

    @Test
    void testCreateUserWithInvalidEmailReturns422() throws Exception {
        var dto = Instancio.of(UserCreateDTO.class)
                .set(Select.field(UserCreateDTO::getEmail), "not-an-email")
                .create();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void testCreateUserWithBlankFirstNameReturns422() throws Exception {
        var dto = Instancio.of(UserCreateDTO.class)
                .set(Select.field(UserCreateDTO::getEmail), "a@example.com")
                .set(Select.field(UserCreateDTO::getFirstName), "")
                .create();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.firstName").exists());
    }

    // ==================== PATCH ====================

    @Test
    void testPatchFirstName() throws Exception {
        var dto = new UserUpdateDTO();
        dto.setFirstName(JsonNullable.of("NewName"));

        mockMvc.perform(patch("/api/users/" + existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("NewName"))
                .andExpect(jsonPath("$.lastName").value(existing.getLastName())); // не тронуто
    }

    @Test
    void testPatchMultipleFields() throws Exception {
        var dto = new UserUpdateDTO();
        dto.setEmail(JsonNullable.of("changed@example.com"));
        dto.setLastName(JsonNullable.of("Changed"));

        mockMvc.perform(patch("/api/users/" + existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("changed@example.com"))
                .andExpect(jsonPath("$.lastName").value("Changed"))
                .andExpect(jsonPath("$.firstName").value(existing.getFirstName())); // не тронуто
    }

    @Test
    void testPatchWithEmptyBodyDoesNothing() throws Exception {
        mockMvc.perform(patch("/api/users/" + existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("existing@example.com"))
                .andExpect(jsonPath("$.firstName").value(existing.getFirstName()));
    }

    @Test
    void testPatchReturns404ForUnknownUser() throws Exception {
        var dto = new UserUpdateDTO();
        dto.setFirstName(JsonNullable.of("X"));

        mockMvc.perform(patch("/api/users/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE ====================

    @Test
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/" + existing.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(existing.getId())).isFalse();
    }

    @Test
    void testDeleteReturns404ForUnknownUser() throws Exception {
        mockMvc.perform(delete("/api/users/99999"))
                .andExpect(status().isNotFound());
    }

    // ==================== FACTORY (Instancio) ====================

    private User newUser(String email) {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .ignore(Select.field(User::getPosts))
                .set(Select.field(User::getEmail), email)
                .create();
    }
}
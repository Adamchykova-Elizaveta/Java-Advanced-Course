package com.advance.integration;

import com.advance.user.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDb() {
        initTokens();
        jdbcTemplate.execute("DELETE FROM payment_cards");
        jdbcTemplate.execute("DELETE FROM users");
    }

    private UserDto buildUserDto(String email) {
        return UserDto.builder()
                .name("Anna")
                .surname("Ivanova")
                .birthDate(LocalDate.of(1995, 5, 15))
                .email(email)
                .build();
    }

    @Test
    void createUser_ShouldReturn201_WhenValidRequest() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUserDto("anna@gmail.com"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Anna"))
                .andExpect(jsonPath("$.email").value("anna@gmail.com"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createUser_ShouldReturn409_WhenEmailAlreadyExists() throws Exception {
        mockMvc.perform(post("/api/users")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildUserDto("anna@gmail.com"))));

        mockMvc.perform(post("/api/users")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUserDto("anna@gmail.com"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("anna@gmail.com")));
    }

    @Test
    void createUser_ShouldReturn400_WhenInvalidRequest() throws Exception {
        UserDto invalid = UserDto.builder().name("").email("not-email").build();

        mockMvc.perform(post("/api/users")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void getById_ShouldReturn200_WhenUserExists() throws Exception {
        String response = mockMvc.perform(post("/api/users")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUserDto("anna@gmail.com"))))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/users/{id}", id)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Anna"));
    }

    @Test
    void getById_ShouldReturn404_WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/99999")
                        .header("Authorization", adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getAll_ShouldReturn200_WithPagination() throws Exception {
        mockMvc.perform(post("/api/users")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildUserDto("anna@gmail.com"))));
        mockMvc.perform(post("/api/users")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildUserDto("kate@gmail.com"))));

        mockMvc.perform(get("/api/users?page=0&size=10")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getAll_ShouldFilterByName() throws Exception {
        mockMvc.perform(post("/api/users")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildUserDto("anna@gmail.com"))));

        mockMvc.perform(get("/api/users?name=Anna&page=0&size=10")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Anna"));
    }

    @Test
    void update_ShouldReturn200_WhenUserExists() throws Exception {
        String response = mockMvc.perform(post("/api/users")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUserDto("anna@gmail.com"))))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();
        UserDto updated = buildUserDto("anna@gmail.com");
        updated.setSurname("Petrova");

        mockMvc.perform(put("/api/users/{id}", id)
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.surname").value("Petrova"));
    }

    @Test
    void deactivate_ShouldReturn204_WhenUserExists() throws Exception {
        String response = mockMvc.perform(post("/api/users")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUserDto("anna@gmail.com"))))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/users/{id}/deactivate", id)
                        .header("Authorization", adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", id)
                        .header("Authorization", adminToken))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void getWithCards_ShouldReturnUserWithEmptyCards() throws Exception {
        String response = mockMvc.perform(post("/api/users")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUserDto("anna@gmail.com"))))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/users/{id}/with-cards", id)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.cards", hasSize(0)));
    }
}
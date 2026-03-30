package com.advance.integration;

import com.advance.dto.PaymentCardDto;
import com.advance.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PaymentCardControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Long userId;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("DELETE FROM payment_cards");
        jdbcTemplate.execute("DELETE FROM users");

        UserDto userDto = UserDto.builder()
                .name("Anna").surname("Ivanova")
                .birthDate(LocalDate.of(1995, 5, 15))
                .email("anna@gmail.com")
                .build();

        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())  // ← добавить чтобы видеть реальную ошибку
                .andReturn().getResponse().getContentAsString();

        userId = objectMapper.readTree(response).get("id").asLong();
    }

    private PaymentCardDto buildCardDto(String number) {
        return PaymentCardDto.builder()
                .userId(userId)
                .number(number)
                .holder("ANNA IVANOVA")
                .expirationDate(LocalDate.of(2027, 12, 1))
                .build();
    }

    @Test
    void createCard_ShouldReturn201_WhenValidRequest() throws Exception {
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCardDto("1234567890123456"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createCard_ShouldReturn400_WhenLimitExceeded() throws Exception {
        for (int i = 0; i < 5; i++) {
            String number = String.format("123456789012345%d", i);
            mockMvc.perform(post("/api/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildCardDto(number))));
        }

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCardDto("1234567890123459"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("5")));
    }

    @Test
    void createCard_ShouldReturn404_WhenUserNotFound() throws Exception {
        PaymentCardDto dto = buildCardDto("1234567890123456");
        dto.setUserId(99999L);

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_ShouldReturn200_WhenCardExists() throws Exception {
        String response = mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCardDto("1234567890123456"))))
                .andReturn().getResponse().getContentAsString();

        Long cardId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/cards/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId));
    }

    @Test
    void getById_ShouldReturn404_WhenCardNotFound() throws Exception {
        mockMvc.perform(get("/api/cards/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getAllByUserId_ShouldReturnCards() throws Exception {
        mockMvc.perform(post("/api/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildCardDto("1234567890123456"))));
        mockMvc.perform(post("/api/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildCardDto("6543210987654321"))));

        mockMvc.perform(get("/api/cards/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void update_ShouldReturn200_WhenCardExists() throws Exception {
        String response = mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCardDto("1234567890123456"))))
                .andReturn().getResponse().getContentAsString();

        Long cardId = objectMapper.readTree(response).get("id").asLong();
        PaymentCardDto updated = buildCardDto("1234567890123456");
        updated.setHolder("UPDATED NAME");

        mockMvc.perform(put("/api/cards/{id}", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holder").value("UPDATED NAME"));
    }

    @Test
    void deactivate_ShouldReturn204_AndCardIsDeactivated() throws Exception {
        String response = mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCardDto("1234567890123456"))))
                .andReturn().getResponse().getContentAsString();

        Long cardId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/cards/{id}/deactivate", cardId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cards/{id}", cardId))
                .andExpect(jsonPath("$.active").value(false));
    }
}
package com.advance.order.integration;

import com.advance.order.dto.ItemDto;
import com.advance.order.dto.OrderDto;
import com.advance.order.dto.OrderItemDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Long itemId;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("DELETE FROM order_items");
        jdbcTemplate.execute("DELETE FROM orders");
        jdbcTemplate.execute("DELETE FROM items");

        wireMock.resetAll();
        wireMock.stubFor(WireMock.get(urlPathMatching("/api/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                              "id": 1,
                              "name": "Anna",
                              "surname": "Ivanova",
                              "email": "anna@gmail.com",
                              "active": true
                            }
                        """)));

        String response = mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                ItemDto.builder().name("Laptop").price(BigDecimal.valueOf(999.99)).build())))
                .andReturn().getResponse().getContentAsString();

        itemId = objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void createOrder_ShouldReturn201() throws Exception {
        OrderDto dto = OrderDto.builder()
                .userId(1L)
                .orderItems(List.of(new OrderItemDto(null, itemId, 2)))
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.user.name").value("Anna"))
                .andExpect(jsonPath("$.totalPrice").value(1999.98));
    }

    @Test
    void getById_ShouldReturn200() throws Exception {
        OrderDto dto = OrderDto.builder()
                .userId(1L)
                .orderItems(List.of(new OrderItemDto(null, itemId, 1)))
                .build();

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.user.email").value("anna@gmail.com"));
    }

    @Test
    void delete_ShouldSoftDelete() throws Exception {
        OrderDto dto = OrderDto.builder()
                .userId(1L)
                .orderItems(List.of(new OrderItemDto(null, itemId, 1)))
                .build();

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
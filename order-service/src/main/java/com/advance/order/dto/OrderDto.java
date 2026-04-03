package com.advance.order.dto;

import com.advance.order.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderDto {
    private Long id;

    @NotNull(message = "User id is required")
    private Long userId;

    private OrderStatus status;
    private BigDecimal totalPrice;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemDto> orderItems;
}
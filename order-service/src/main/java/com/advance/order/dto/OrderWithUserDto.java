package com.advance.order.dto;

import com.advance.order.entity.OrderStatus;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderWithUserDto {
    private Long id;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private List<OrderItemDto> orderItems;
    private UserDto user;
}
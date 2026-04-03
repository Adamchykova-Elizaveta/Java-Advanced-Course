package com.advance.order.mapper;

import com.advance.order.dto.OrderDto;
import com.advance.order.dto.OrderItemDto;
import com.advance.order.entity.Order;
import com.advance.order.entity.OrderItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "orderItems", source = "orderItems")
    OrderDto toDto(Order order);

    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Order toEntity(OrderDto dto);

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "id", source = "id")
    OrderItemDto toItemDto(OrderItem orderItem);
}
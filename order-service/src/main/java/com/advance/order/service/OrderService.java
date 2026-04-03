package com.advance.order.service;

import com.advance.order.client.UserServiceClient;
import com.advance.order.dto.*;
import com.advance.order.entity.*;
import com.advance.order.exception.EntityNotFoundException;
import com.advance.order.mapper.OrderMapper;
import com.advance.order.repository.ItemRepository;
import com.advance.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient;

    public List<OrderWithUserDto> getAll() {
        return orderRepository.findAllByDeletedFalse().stream()
                .map(this::toOrderWithUser)
                .toList();
    }

    public OrderWithUserDto getById(Long id) {
        Order order = findById(id);
        return toOrderWithUser(order);
    }

    public List<OrderWithUserDto> getByUserId(Long userId) {
        return orderRepository.findAllByUserIdAndDeletedFalse(userId).stream()
                .map(this::toOrderWithUser)
                .toList();
    }

    @Transactional
    public OrderWithUserDto create(OrderDto dto) {
        Order order = Order.builder()
                .userId(dto.getUserId())
                .status(OrderStatus.PENDING)
                .deleted(false)
                .orderItems(new ArrayList<>())
                .build();

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> items = dto.getOrderItems().stream().map(itemDto -> {
            Item item = itemRepository.findById(itemDto.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException("Item", itemDto.getItemId()));
            return OrderItem.builder()
                    .order(savedOrder)
                    .item(item)
                    .quantity(itemDto.getQuantity())
                    .build();
        }).collect(Collectors.toList());

        savedOrder.setOrderItems(items);
        savedOrder.setTotalPrice(calculateTotal(items));

        return toOrderWithUser(orderRepository.save(savedOrder));
    }

    @Transactional
    public OrderWithUserDto update(Long id, OrderDto dto) {
        Order order = findById(id);
        order.setStatus(dto.getStatus());

        if (dto.getOrderItems() != null && !dto.getOrderItems().isEmpty()) {
            order.getOrderItems().clear();

            List<OrderItem> items = dto.getOrderItems().stream().map(itemDto -> {
                Item item = itemRepository.findById(itemDto.getItemId())
                        .orElseThrow(() -> new EntityNotFoundException("Item", itemDto.getItemId()));
                return OrderItem.builder()
                        .order(order)
                        .item(item)
                        .quantity(itemDto.getQuantity())
                        .build();
            }).collect(Collectors.toList());

            order.getOrderItems().addAll(items);
            order.setTotalPrice(calculateTotal(items));
        }

        return toOrderWithUser(orderRepository.save(order));
    }

    @Transactional
    public void delete(Long id) {
        Order order = findById(id);
        order.setDeleted(true);
        orderRepository.save(order);
    }

    private Order findById(Long id) {
        return orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Order", id));
    }

    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(i -> i.getItem().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderWithUserDto toOrderWithUser(Order order) {
        UserDto user = userServiceClient.getUserById(order.getUserId());
        OrderDto orderDto = orderMapper.toDto(order);
        return OrderWithUserDto.builder()
                .id(orderDto.getId())
                .status(orderDto.getStatus())
                .totalPrice(orderDto.getTotalPrice())
                .orderItems(orderDto.getOrderItems())
                .user(user)
                .build();
    }
}
package com.advance.order.service;

import com.advance.order.client.UserServiceClient;
import com.advance.order.dto.*;
import com.advance.order.entity.*;
import com.advance.order.exception.EntityNotFoundException;
import com.advance.order.mapper.OrderMapper;
import com.advance.order.repository.ItemRepository;
import com.advance.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private UserServiceClient userServiceClient;

    @InjectMocks private OrderService orderService;

    private Item item;
    private Order order;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setPrice(BigDecimal.valueOf(10.00));

        order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(BigDecimal.valueOf(20.00));
        order.setDeleted(false);

        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setQuantity(2);
        orderItem.setOrder(order);
        order.setOrderItems(List.of(orderItem));

        userDto = UserDto.builder()
                .id(1L).name("Anna").surname("Ivanova").email("anna@gmail.com")
                .build();
    }

    @Test
    void getAll_ShouldReturnOrdersWithUsers() {
        when(orderRepository.findAllByDeletedFalse()).thenReturn(List.of(order));
        when(userServiceClient.getUserById(1L)).thenReturn(userDto);
        when(orderMapper.toDto(any())).thenReturn(buildOrderDto());

        List<OrderWithUserDto> result = orderService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getName()).isEqualTo("Anna");
    }

    @Test
    void getById_ShouldReturnOrderWithUser() {
        when(orderRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(order));
        when(userServiceClient.getUserById(1L)).thenReturn(userDto);
        when(orderMapper.toDto(any())).thenReturn(buildOrderDto());

        OrderWithUserDto result = orderService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getUser().getEmail()).isEqualTo("anna@gmail.com");
    }

    @Test
    void getById_ShouldThrow_WhenNotFound() {
        when(orderRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_ShouldCreateOrder() {
        OrderItemDto itemDto = new OrderItemDto(null, 1L, 2);
        OrderDto dto = OrderDto.builder()
                .userId(1L)
                .orderItems(List.of(itemDto))
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepository.save(any())).thenReturn(order);
        when(userServiceClient.getUserById(1L)).thenReturn(userDto);
        when(orderMapper.toDto(any())).thenReturn(buildOrderDto());

        OrderWithUserDto result = orderService.create(dto);

        assertThat(result).isNotNull();
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void create_ShouldThrow_WhenItemNotFound() {
        OrderItemDto itemDto = new OrderItemDto(null, 99L, 1);
        OrderDto dto = OrderDto.builder().userId(1L).orderItems(List.of(itemDto)).build();

        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_ShouldSoftDelete() {
        when(orderRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        orderService.delete(1L);

        assertThat(order.getDeleted()).isTrue();
        verify(orderRepository).save(order);
    }

    @Test
    void delete_ShouldThrow_WhenNotFound() {
        when(orderRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private OrderDto buildOrderDto() {
        return OrderDto.builder()
                .id(1L).userId(1L).status(OrderStatus.PENDING)
                .totalPrice(BigDecimal.valueOf(20.00))
                .orderItems(List.of(new OrderItemDto(1L, 1L, 2)))
                .build();
    }
}
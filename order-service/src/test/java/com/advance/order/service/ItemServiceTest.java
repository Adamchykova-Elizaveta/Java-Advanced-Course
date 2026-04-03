package com.advance.order.service;

import com.advance.order.dto.ItemDto;
import com.advance.order.entity.Item;
import com.advance.order.exception.EntityNotFoundException;
import com.advance.order.mapper.ItemMapper;
import com.advance.order.repository.ItemRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock private ItemRepository itemRepository;
    @Mock private ItemMapper itemMapper;

    @InjectMocks private ItemService itemService;

    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setId(1L);
        item.setName("Laptop");
        item.setPrice(BigDecimal.valueOf(999.99));

        itemDto = ItemDto.builder()
                .id(1L).name("Laptop").price(BigDecimal.valueOf(999.99)).build();
    }

    @Test
    void getAll_ShouldReturnAllItems() {
        when(itemRepository.findAll()).thenReturn(List.of(item));
        when(itemMapper.toDto(any())).thenReturn(itemDto);

        List<ItemDto> result = itemService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Laptop");
    }

    @Test
    void getById_ShouldReturnItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(any())).thenReturn(itemDto);

        ItemDto result = itemService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getById_ShouldThrow_WhenNotFound() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_ShouldSaveAndReturnItem() {
        when(itemMapper.toEntity(any())).thenReturn(item);
        when(itemRepository.save(any())).thenReturn(item);
        when(itemMapper.toDto(any())).thenReturn(itemDto);

        ItemDto result = itemService.create(itemDto);

        assertThat(result.getName()).isEqualTo("Laptop");
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void update_ShouldUpdateItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(item);
        when(itemMapper.toDto(any())).thenReturn(itemDto);

        ItemDto result = itemService.update(1L, itemDto);

        assertThat(result).isNotNull();
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void delete_ShouldDeleteItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        itemService.delete(1L);

        verify(itemRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrow_WhenNotFound() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
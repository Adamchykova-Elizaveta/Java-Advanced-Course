package com.advance.order.service;

import com.advance.order.dto.ItemDto;
import com.advance.order.entity.Item;
import com.advance.order.exception.EntityNotFoundException;
import com.advance.order.mapper.ItemMapper;
import com.advance.order.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    public List<ItemDto> getAll() {
        return itemRepository.findAll().stream().map(itemMapper::toDto).toList();
    }

    public ItemDto getById(Long id) {
        return itemMapper.toDto(findById(id));
    }

    @Transactional
    public ItemDto create(ItemDto dto) {
        Item item = itemMapper.toEntity(dto);
        return itemMapper.toDto(itemRepository.save(item));
    }

    @Transactional
    public ItemDto update(Long id, ItemDto dto) {
        Item item = findById(id);
        item.setName(dto.getName());
        item.setPrice(dto.getPrice());
        return itemMapper.toDto(itemRepository.save(item));
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        itemRepository.deleteById(id);
    }

    private Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item", id));
    }
}
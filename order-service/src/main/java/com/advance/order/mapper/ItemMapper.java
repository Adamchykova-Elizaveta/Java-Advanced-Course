package com.advance.order.mapper;

import com.advance.order.dto.ItemDto;
import com.advance.order.entity.Item;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDto toDto(Item item);

    @Mapping(target = "orderItems", ignore = true)
    Item toEntity(ItemDto dto);
}
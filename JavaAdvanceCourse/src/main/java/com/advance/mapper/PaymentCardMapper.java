package com.advance.mapper;

import com.advance.dto.PaymentCardDto;
import com.advance.entity.PaymentCard;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

    @Mapping(source = "user.id", target = "userId")
    PaymentCardDto toDto(PaymentCard card);

    @Mapping(target = "user", ignore = true)
    PaymentCard toEntity(PaymentCardDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    void updateEntity(PaymentCardDto dto, @MappingTarget PaymentCard card);
}
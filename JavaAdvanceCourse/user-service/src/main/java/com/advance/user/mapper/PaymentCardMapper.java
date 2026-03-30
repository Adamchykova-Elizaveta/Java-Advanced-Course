package com.advance.user.mapper;

import com.advance.user.dto.PaymentCardDto;
import com.advance.user.entity.PaymentCard;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

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
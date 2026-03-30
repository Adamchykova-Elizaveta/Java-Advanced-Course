package com.advance.user.mapper;

import com.advance.user.dto.UserDto;
import com.advance.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    @Mapping(target = "paymentCards", ignore = true)
    User toEntity(UserDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "paymentCards", ignore = true)
    void updateEntity(UserDto dto, @MappingTarget User user);
}
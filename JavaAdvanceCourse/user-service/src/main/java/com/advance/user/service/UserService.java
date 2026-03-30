package com.advance.user.service;

import com.advance.user.dto.PageResponse;
import com.advance.user.dto.PaymentCardDto;
import com.advance.user.dto.UserDto;
import com.advance.user.dto.UserWithCardsDto;
import com.advance.user.entity.User;
import com.advance.user.exception.DuplicateEmailException;
import com.advance.user.exception.EntityNotFoundException;
import com.advance.user.mapper.PaymentCardMapper;
import com.advance.user.mapper.UserMapper;
import com.advance.user.repository.PaymentCardRepository;
import com.advance.user.repository.UserRepository;
import com.advance.user.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PaymentCardRepository paymentCardRepository;
    private final UserMapper userMapper;
    private final PaymentCardMapper paymentCardMapper;

    @Transactional
    public UserDto create(UserDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateEmailException(dto.getEmail());
        }
        User user = userMapper.toEntity(dto);
        user.setActive(true);
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public UserDto getById(Long id) {
        return userMapper.toDto(findById(id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users_with_cards", key = "#id")
    public UserWithCardsDto getByIdWithCards(Long id) {
        User user = findById(id);
        List<PaymentCardDto> cards = paymentCardRepository.findAllByUserId(id)
                .stream().map(paymentCardMapper::toDto).toList();

        return UserWithCardsDto.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .birthDate(user.getBirthDate())
                .email(user.getEmail())
                .active(user.getActive())
                .cards(cards)
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<UserDto> getAll(String name, String surname, Boolean active, int page, int size) {
        Specification<User> spec = Specification
                .where(UserSpecification.hasName(name))
                .and(UserSpecification.hasSurname(surname))
                .and(UserSpecification.isActive(active));

        Page<User> result = userRepository.findAll(spec, PageRequest.of(page, size));

        return PageResponse.<UserDto>builder()
                .content(result.getContent().stream().map(userMapper::toDto).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    @Transactional
    @CacheEvict(value = "users_with_cards", key = "#id")
    @CachePut(value = "users", key = "#id")
    public UserDto update(Long id, UserDto dto) {
        User user = findById(id);
        if (!user.getEmail().equals(dto.getEmail()) &&
                userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateEmailException(dto.getEmail());
        }
        userMapper.updateEntity(dto, user);
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    @CacheEvict(value = {"users", "users_with_cards"}, key = "#id")
    public void setActiveStatus(Long id, Boolean active) {
        findById(id);
        userRepository.updateActiveStatus(id, active);
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
    }
}
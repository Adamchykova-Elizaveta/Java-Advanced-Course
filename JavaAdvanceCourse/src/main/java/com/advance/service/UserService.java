package com.advance.service;

import com.advance.dto.PageResponse;
import com.advance.dto.UserDto;
import com.advance.entity.User;
import com.advance.repository.UserRepository;
import com.advance.specification.UserSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDto create(UserDto dto) {
        User user = User.builder()
                .name(dto.getName())
                .surname(dto.getSurname())
                .birthDate(dto.getBirthDate())
                .email(dto.getEmail())
                .active(true)
                .build();
        return toDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        return toDto(findById(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserDto> getAll(String name, String surname, Boolean active, int page, int size) {
        Specification<User> spec = Specification
                .where(UserSpecification.hasName(name))
                .and(UserSpecification.hasSurname(surname))
                .and(UserSpecification.isActive(active));

        Page<User> result = userRepository.findAll(spec, PageRequest.of(page, size));

        return PageResponse.<UserDto>builder()
                .content(result.getContent().stream().map(this::toDto).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    @Transactional
    public UserDto update(Long id, UserDto dto) {
        User user = findById(id);
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setBirthDate(dto.getBirthDate());
        user.setEmail(dto.getEmail());
        return toDto(userRepository.save(user));
    }

    @Transactional
    public void setActiveStatus(Long id, Boolean active) {
        findById(id);
        userRepository.updateActiveStatus(id, active);
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .birthDate(user.getBirthDate())
                .email(user.getEmail())
                .active(user.getActive())
                .build();
    }
}
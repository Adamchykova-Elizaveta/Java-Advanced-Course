package com.advance.service;

import com.advance.user.dto.PageResponse;
import com.advance.user.dto.UserDto;
import com.advance.user.dto.UserWithCardsDto;
import com.advance.user.entity.User;
import com.advance.user.exception.DuplicateEmailException;
import com.advance.user.exception.EntityNotFoundException;
import com.advance.user.mapper.PaymentCardMapper;
import com.advance.user.mapper.UserMapper;
import com.advance.user.repository.PaymentCardRepository;
import com.advance.user.repository.UserRepository;
import com.advance.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PaymentCardRepository paymentCardRepository;
    @Mock private UserMapper userMapper;
    @Mock private PaymentCardMapper paymentCardMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Anna");
        user.setSurname("Ivanova");
        user.setEmail("anna@gmail.com");
        user.setBirthDate(LocalDate.of(1995, 5, 15));
        user.setActive(true);

        userDto = UserDto.builder()
                .id(1L)
                .name("Anna")
                .surname("Ivanova")
                .email("anna@gmail.com")
                .birthDate(LocalDate.of(1995, 5, 15))
                .active(true)
                .build();
    }

    @Test
    void create_ShouldReturnUserDto_WhenEmailIsUnique() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userMapper.toEntity(any())).thenReturn(user);
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toDto(any())).thenReturn(userDto);

        UserDto result = userService.create(userDto);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("anna@gmail.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_ShouldThrowDuplicateEmailException_WhenEmailExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.create(userDto))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("anna@gmail.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void getById_ShouldReturnUserDto_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(any())).thenReturn(userDto);

        UserDto result = userService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getById_ShouldThrowEntityNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getByIdWithCards_ShouldReturnUserWithCards() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentCardRepository.findAllByUserId(1L)).thenReturn(List.of());

        UserWithCardsDto result = userService.getByIdWithCards(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCards()).isEmpty();
    }

    @Test
    void getAll_ShouldReturnPageResponse() {
        var page = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
        when(userRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);
        when(userMapper.toDto(any())).thenReturn(userDto);

        PageResponse<UserDto> result = userService.getAll(null, null, null, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    void getAll_ShouldReturnFilteredByName() {
        var page = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
        when(userRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);
        when(userMapper.toDto(any())).thenReturn(userDto);

        PageResponse<UserDto> result = userService.getAll("Anna", null, null, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Anna");
    }

    @Test
    void update_ShouldReturnUpdatedUserDto() {
        UserDto updatedDto = UserDto.builder()
                .name("Anna")
                .surname("Petrova")
                .email("anna@gmail.com")
                .birthDate(LocalDate.of(1995, 5, 15))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toDto(any())).thenReturn(updatedDto);

        UserDto result = userService.update(1L, updatedDto);

        assertThat(result.getSurname()).isEqualTo("Petrova");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void update_ShouldThrowDuplicateEmailException_WhenNewEmailAlreadyTaken() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("other@gmail.com");

        UserDto dtoWithNewEmail = UserDto.builder()
                .email("other@gmail.com")
                .name("Anna")
                .surname("Ivanova")
                .birthDate(LocalDate.of(1995, 5, 15))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("other@gmail.com")).thenReturn(Optional.of(anotherUser));

        assertThatThrownBy(() -> userService.update(1L, dtoWithNewEmail))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void setActiveStatus_ShouldDeactivateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.setActiveStatus(1L, false);

        verify(userRepository).updateActiveStatus(1L, false);
    }

    @Test
    void setActiveStatus_ShouldThrowEntityNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.setActiveStatus(99L, false))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
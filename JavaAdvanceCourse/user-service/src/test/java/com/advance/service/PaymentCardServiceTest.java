package com.advance.service;

import com.advance.user.dto.PageResponse;
import com.advance.user.dto.PaymentCardDto;
import com.advance.user.entity.PaymentCard;
import com.advance.user.entity.User;
import com.advance.user.exception.CardLimitExceededException;
import com.advance.user.exception.EntityNotFoundException;
import com.advance.user.mapper.PaymentCardMapper;
import com.advance.user.repository.PaymentCardRepository;
import com.advance.user.repository.UserRepository;
import com.advance.user.service.PaymentCardService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceTest {

    @Mock private PaymentCardRepository paymentCardRepository;
    @Mock private UserRepository userRepository;
    @Mock private PaymentCardMapper paymentCardMapper;

    @InjectMocks
    private PaymentCardService paymentCardService;

    private User user;
    private PaymentCard card;
    private PaymentCardDto cardDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Anna");
        user.setEmail("anna@gmail.com");
        user.setActive(true);

        card = new PaymentCard();
        card.setId(1L);
        card.setUser(user);
        card.setNumber("1234567890123456");
        card.setHolder("ANNA IVANOVA");
        card.setExpirationDate(LocalDate.of(2027, 12, 1));
        card.setActive(true);

        cardDto = PaymentCardDto.builder()
                .id(1L)
                .userId(1L)
                .number("1234567890123456")
                .holder("ANNA IVANOVA")
                .expirationDate(LocalDate.of(2027, 12, 1))
                .active(true)
                .build();
    }

    @Test
    void create_ShouldReturnCardDto_WhenUnderLimit() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserId(1L)).thenReturn(2);
        when(paymentCardMapper.toEntity(any())).thenReturn(card);
        when(paymentCardRepository.save(any())).thenReturn(card);
        when(paymentCardMapper.toDto(any())).thenReturn(cardDto);

        PaymentCardDto result = paymentCardService.create(cardDto);

        assertThat(result).isNotNull();
        assertThat(result.getNumber()).isEqualTo("1234567890123456");
        verify(paymentCardRepository).save(any(PaymentCard.class));
    }

    @Test
    void create_ShouldThrowCardLimitExceededException_WhenLimitReached() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserId(1L)).thenReturn(5);

        assertThatThrownBy(() -> paymentCardService.create(cardDto))
                .isInstanceOf(CardLimitExceededException.class)
                .hasMessageContaining("5");

        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowEntityNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        cardDto.setUserId(99L);

        assertThatThrownBy(() -> paymentCardService.create(cardDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getById_ShouldReturnCardDto_WhenCardExists() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(paymentCardMapper.toDto(any())).thenReturn(cardDto);

        PaymentCardDto result = paymentCardService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getById_ShouldThrowEntityNotFoundException_WhenCardNotFound() {
        when(paymentCardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentCardService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_ShouldReturnPageResponse() {
        var page = new PageImpl<>(List.of(card), PageRequest.of(0, 10), 1);
        when(paymentCardRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);
        when(paymentCardMapper.toDto(any())).thenReturn(cardDto);

        PageResponse<PaymentCardDto> result = paymentCardService.getAll(null, null, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getAllByUserId_ShouldReturnCards_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentCardRepository.findAllByUserId(1L)).thenReturn(List.of(card));
        when(paymentCardMapper.toDto(any())).thenReturn(cardDto);

        List<PaymentCardDto> result = paymentCardService.getAllByUserId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllByUserId_ShouldThrowEntityNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentCardService.getAllByUserId(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_ShouldReturnUpdatedCardDto() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(paymentCardRepository.save(any())).thenReturn(card);
        when(paymentCardMapper.toDto(any())).thenReturn(cardDto);

        PaymentCardDto result = paymentCardService.update(1L, cardDto);

        assertThat(result).isNotNull();
        verify(paymentCardRepository).save(any(PaymentCard.class));
    }

    @Test
    void setActiveStatus_ShouldDeactivateCard() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));

        paymentCardService.setActiveStatus(1L, false);

        verify(paymentCardRepository).updateActiveStatus(1L, false);
    }

    @Test
    void setActiveStatus_ShouldThrowEntityNotFoundException_WhenCardNotFound() {
        when(paymentCardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentCardService.setActiveStatus(99L, false))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
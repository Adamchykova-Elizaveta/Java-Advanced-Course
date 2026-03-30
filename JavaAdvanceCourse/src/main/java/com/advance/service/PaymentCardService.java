package com.advance.service;

import com.advance.dto.PageResponse;
import com.advance.dto.PaymentCardDto;
import com.advance.entity.PaymentCard;
import com.advance.entity.User;
import com.advance.exception.CardLimitExceededException;
import com.advance.exception.EntityNotFoundException;
import com.advance.mapper.PaymentCardMapper;
import com.advance.repository.PaymentCardRepository;
import com.advance.repository.UserRepository;
import com.advance.specification.PaymentCardSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentCardService {

    private static final int MAX_CARDS_PER_USER = 5;

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;

    @Transactional
    @CacheEvict(value = "users_with_cards", key = "#dto.userId")
    public PaymentCardDto create(PaymentCardDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User", dto.getUserId()));

        int cardCount = paymentCardRepository.countByUserIdAndActiveTrue(dto.getUserId());
        if (cardCount >= MAX_CARDS_PER_USER) {
            throw new CardLimitExceededException(dto.getUserId(), MAX_CARDS_PER_USER);
        }

        PaymentCard card = paymentCardMapper.toEntity(dto);
        card.setUser(user);
        card.setActive(true);
        return paymentCardMapper.toDto(paymentCardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public PaymentCardDto getById(Long id) {
        return paymentCardMapper.toDto(findById(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentCardDto> getAll(String holder, Boolean active, int page, int size) {
        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecification.hasHolder(holder))
                .and(PaymentCardSpecification.isActive(active));

        Page<PaymentCard> result = paymentCardRepository.findAll(spec, PageRequest.of(page, size));

        return PageResponse.<PaymentCardDto>builder()
                .content(result.getContent().stream().map(paymentCardMapper::toDto).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public List<PaymentCardDto> getAllByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
        return paymentCardRepository.findAllByUserId(userId)
                .stream().map(paymentCardMapper::toDto).toList();
    }

    @Transactional
    @CacheEvict(value = "users_with_cards", key = "#result.userId")
    public PaymentCardDto update(Long id, PaymentCardDto dto) {
        PaymentCard card = findById(id);
        paymentCardMapper.updateEntity(dto, card);
        return paymentCardMapper.toDto(paymentCardRepository.save(card));
    }

    @Transactional
    public void setActiveStatus(Long id, Boolean active) {
        PaymentCard card = findById(id);
        paymentCardRepository.updateActiveStatus(id, active);
        evictUserWithCardsCache(card.getUser().getId());
    }

    @CacheEvict(value = "users_with_cards", key = "#userId")
    public void evictUserWithCardsCache(Long userId) {}

    private PaymentCard findById(Long id) {
        return paymentCardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card", id));
    }
}
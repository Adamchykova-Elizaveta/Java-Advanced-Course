package com.advance.user.controller;

import com.advance.user.dto.PageResponse;
import com.advance.user.dto.PaymentCardDto;
import com.advance.user.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @PostMapping
    public ResponseEntity<PaymentCardDto> create(@Valid @RequestBody PaymentCardDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCardService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentCardService.getById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<PaymentCardDto>> getAll(
            @RequestParam(required = false) String holder,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(paymentCardService.getAll(holder, active, page, size));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentCardDto>> getAllByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentCardService.getAllByUserId(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardDto> update(@PathVariable Long id, @Valid @RequestBody PaymentCardDto dto) {
        return ResponseEntity.ok(paymentCardService.update(id, dto));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        paymentCardService.setActiveStatus(id, true);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        paymentCardService.setActiveStatus(id, false);
        return ResponseEntity.noContent().build();
    }
}
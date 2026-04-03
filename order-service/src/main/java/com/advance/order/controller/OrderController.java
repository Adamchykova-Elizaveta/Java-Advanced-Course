package com.advance.order.controller;

import com.advance.order.dto.OrderDto;
import com.advance.order.dto.OrderWithUserDto;
import com.advance.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderWithUserDto>> getAll() {
        return ResponseEntity.ok(orderService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderWithUserDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderWithUserDto>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<OrderWithUserDto> create(@Valid @RequestBody OrderDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderWithUserDto> update(@PathVariable Long id, @Valid @RequestBody OrderDto dto) {
        return ResponseEntity.ok(orderService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
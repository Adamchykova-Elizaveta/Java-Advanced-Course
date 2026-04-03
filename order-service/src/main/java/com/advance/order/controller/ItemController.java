package com.advance.order.controller;

import com.advance.order.dto.ItemDto;
import com.advance.order.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<List<ItemDto>> getAll() {
        return ResponseEntity.ok(itemService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ItemDto> create(@Valid @RequestBody ItemDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemDto> update(@PathVariable Long id, @Valid @RequestBody ItemDto dto) {
        return ResponseEntity.ok(itemService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
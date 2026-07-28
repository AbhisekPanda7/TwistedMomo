package com.twistedmomos.backend.controller;

import com.twistedmomos.backend.dto.response.MenuItemResponse;
import com.twistedmomos.backend.dto.response.PageResponse;
import com.twistedmomos.backend.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuItemService menuItemService;

    @GetMapping
    public ResponseEntity<PageResponse<MenuItemResponse>> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean veg,
            @RequestParam(required = false) Integer maxSpicy,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(menuItemService.search(categoryId, q, veg, maxSpicy, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(menuItemService.getPublicById(id));
    }
}

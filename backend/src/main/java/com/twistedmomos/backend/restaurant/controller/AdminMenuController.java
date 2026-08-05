package com.twistedmomos.backend.restaurant.controller;

import com.twistedmomos.backend.restaurant.dto.request.MenuItemRequest;
import com.twistedmomos.backend.restaurant.dto.response.MenuItemResponse;
import com.twistedmomos.backend.shared.dto.response.PageResponse;
import com.twistedmomos.backend.restaurant.service.MenuItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Every endpoint here requires ROLE_ADMIN — enforced in SecurityConfig via the /api/v1/admin/** matcher. */
@Tag(name = "Admin — Menu", description = "Menu item management and image upload (ROLE_ADMIN)")
@RestController
@RequestMapping("/api/v1/admin/menu")
@RequiredArgsConstructor
public class AdminMenuController {

    private final MenuItemService menuItemService;

    @GetMapping
    public ResponseEntity<PageResponse<MenuItemResponse>> list(
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(menuItemService.adminList(categoryId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(menuItemService.getAdminById(id));
    }

    @PostMapping
    public ResponseEntity<MenuItemResponse> create(@Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuItemService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItemResponse> update(@PathVariable Long id, @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuItemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        menuItemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/image", consumes = "multipart/form-data")
    public ResponseEntity<MenuItemResponse> uploadImage(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(menuItemService.uploadImage(id, file));
    }
}

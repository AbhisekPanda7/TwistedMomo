package com.twistedmomos.backend.restaurant.controller;

import com.twistedmomos.backend.restaurant.dto.request.AvailabilityRequest;
import com.twistedmomos.backend.restaurant.dto.response.MenuItemResponse;
import com.twistedmomos.backend.restaurant.service.MenuItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Marking a dish sold out is order operations, not menu editing — an employee needs it
 * mid-service. Everything else on the menu stays ADMIN-only.
 */
@Tag(name = "Ops — Menu", description = "Item availability (ROLE_ADMIN, ROLE_RESTAURANT_EMP)")
@RestController
@RequestMapping("/api/v1/ops/menu")
@RequiredArgsConstructor
public class OpsMenuController {

    private final MenuItemService menuItemService;

    @PatchMapping("/{id}/availability")
    public ResponseEntity<MenuItemResponse> setAvailability(
            @PathVariable Long id, @Valid @RequestBody AvailabilityRequest request) {
        return ResponseEntity.ok(menuItemService.setAvailability(id, request.available()));
    }
}

package com.twistedmomos.backend.controller;

import com.twistedmomos.backend.dto.request.AddCartItemRequest;
import com.twistedmomos.backend.dto.request.UpdateCartItemRequest;
import com.twistedmomos.backend.dto.response.CartResponse;
import com.twistedmomos.backend.security.CustomUserDetails;
import com.twistedmomos.backend.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Every endpoint here operates on the authenticated caller's own cart — there is no cart-by-id lookup. */
@Tag(name = "Cart", description = "The signed-in user's cart")
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getUser().getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(principal.getUser().getId(), request));
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(principal.getUser().getId(), itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(principal.getUser().getId(), itemId));
    }

    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(cartService.clearCart(principal.getUser().getId()));
    }
}

package com.twistedmomos.backend.service;

import com.twistedmomos.backend.dto.request.AddCartItemRequest;
import com.twistedmomos.backend.dto.request.UpdateCartItemRequest;
import com.twistedmomos.backend.dto.response.CartResponse;

public interface CartService {

    CartResponse getCart(Long userId);

    CartResponse addItem(Long userId, AddCartItemRequest request);

    CartResponse updateItem(Long userId, Long cartItemId, UpdateCartItemRequest request);

    CartResponse removeItem(Long userId, Long cartItemId);

    CartResponse clearCart(Long userId);
}

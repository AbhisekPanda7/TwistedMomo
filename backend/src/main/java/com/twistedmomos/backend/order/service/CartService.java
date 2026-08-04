package com.twistedmomos.backend.order.service;

import com.twistedmomos.backend.order.dto.request.AddCartItemRequest;
import com.twistedmomos.backend.order.dto.request.UpdateCartItemRequest;
import com.twistedmomos.backend.order.dto.response.CartResponse;

public interface CartService {

    CartResponse getCart(Long userId);

    CartResponse addItem(Long userId, AddCartItemRequest request);

    CartResponse updateItem(Long userId, Long cartItemId, UpdateCartItemRequest request);

    CartResponse removeItem(Long userId, Long cartItemId);

    CartResponse clearCart(Long userId);
}

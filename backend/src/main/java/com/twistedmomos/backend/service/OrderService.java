package com.twistedmomos.backend.service;

import com.twistedmomos.backend.dto.request.PlaceOrderRequest;
import com.twistedmomos.backend.dto.response.OrderResponse;
import com.twistedmomos.backend.dto.response.OrderSummaryResponse;
import com.twistedmomos.backend.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse placeOrder(Long userId, PlaceOrderRequest request);

    PageResponse<OrderSummaryResponse> listMyOrders(Long userId, Pageable pageable);

    OrderResponse getMyOrder(Long userId, Long orderId);

    OrderResponse cancelMyOrder(Long userId, Long orderId);

    PageResponse<OrderSummaryResponse> listAllOrders(String statusFilter, Pageable pageable);

    OrderResponse getOrder(Long orderId);

    OrderResponse updateStatus(Long orderId, String status);
}

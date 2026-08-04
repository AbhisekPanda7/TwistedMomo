package com.twistedmomos.backend.order.service;

import com.twistedmomos.backend.order.dto.request.PlaceOrderRequest;
import com.twistedmomos.backend.order.dto.response.OrderResponse;
import com.twistedmomos.backend.order.dto.response.OrderSummaryResponse;
import com.twistedmomos.backend.shared.dto.response.PageResponse;
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

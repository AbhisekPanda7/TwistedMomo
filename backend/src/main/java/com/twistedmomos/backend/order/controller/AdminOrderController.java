package com.twistedmomos.backend.order.controller;

import com.twistedmomos.backend.order.dto.request.UpdateOrderStatusRequest;
import com.twistedmomos.backend.order.dto.response.OrderResponse;
import com.twistedmomos.backend.order.dto.response.OrderSummaryResponse;
import com.twistedmomos.backend.shared.dto.response.PageResponse;
import com.twistedmomos.backend.order.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Every endpoint here requires ROLE_ADMIN — enforced in SecurityConfig via the /api/v1/admin/** matcher. */
@Tag(name = "Admin — Orders", description = "Order listing and status transitions (ROLE_ADMIN)")
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<PageResponse<OrderSummaryResponse>> list(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(orderService.listAllOrders(status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request.status()));
    }
}

package com.storeconnect.order.controller;

import com.storeconnect.order.dto.CheckoutRequest;
import com.storeconnect.order.dto.OrderDTO;
import com.storeconnect.order.dto.OrderStatusUpdateRequest;
import com.storeconnect.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Orders", description = "Checkout, Order tracking, Cancellation, and Fulfillment status updates")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Checkout active shopping cart and place order")
    @PostMapping("/checkout")
    public ResponseEntity<OrderDTO> checkout(
            @RequestHeader("X-User-Id") String customerId,
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.checkout(customerId, request));
    }

    @Operation(summary = "Get order history for authenticated customer")
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getMyOrders(
            @RequestHeader("X-User-Id") String customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @Operation(summary = "Get order details by ID or order number")
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String customerId,
            @RequestHeader(value = "X-User-Role", defaultValue = "CUSTOMER") String userRole) {
        return ResponseEntity.ok(orderService.getOrderById(id, customerId, userRole));
    }

    @Operation(summary = "Cancel an order (only if still in CREATED status)")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String customerId,
            @RequestHeader(value = "X-User-Role", defaultValue = "CUSTOMER") String userRole) {
        return ResponseEntity.ok(orderService.cancelOrder(id, customerId, userRole));
    }

    @Operation(summary = "Update order fulfillment status (Store Operator or Admin)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('STORE_OPERATOR', 'ADMIN')")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable String id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request));
    }

    @Operation(summary = "Get incoming orders for a store (Store Operator or Admin)")
    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasAnyRole('STORE_OPERATOR', 'ADMIN')")
    public ResponseEntity<List<OrderDTO>> getStoreOrders(@PathVariable String storeId) {
        return ResponseEntity.ok(orderService.getOrdersByStore(storeId));
    }
}

package com.storeconnect.order.controller;

import com.storeconnect.order.dto.CheckoutRequest;
import com.storeconnect.order.dto.OrderDTO;
import com.storeconnect.order.dto.UpdateOrderStatusRequest;
import com.storeconnect.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderDTO> checkout(@RequestHeader("X-User-Id") String customerId, @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.checkout(customerId, request));
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getMyOrders(@RequestHeader("X-User-Id") String customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderDTO>> getAllOrders(@RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equalsIgnoreCase(role) && !"STORE_OPERATOR".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByStore(@PathVariable("storeId") String storeId,
                                                          @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equalsIgnoreCase(role) && !"STORE_OPERATOR".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(orderService.getOrdersByStore(storeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable("id") String id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable("id") String id,
                                                      @RequestHeader(value = "X-User-Role", required = false) String role,
                                                      @Valid @RequestBody UpdateOrderStatusRequest request) {
        if (!"ADMIN".equalsIgnoreCase(role) && !"STORE_OPERATOR".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus()));
    }
}

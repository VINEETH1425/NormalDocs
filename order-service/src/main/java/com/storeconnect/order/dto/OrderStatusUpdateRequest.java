package com.storeconnect.order.dto;

import com.storeconnect.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
    @NotNull(message = "New order status is required")
    OrderStatus status,
    String comment
) {}

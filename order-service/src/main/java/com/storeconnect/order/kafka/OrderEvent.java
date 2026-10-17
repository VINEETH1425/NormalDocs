package com.storeconnect.order.kafka;

import java.math.BigDecimal;
import java.util.List;

public record OrderEvent(
    String eventType, // ORDER_PLACED, ORDER_CANCELLED, ORDER_CONFIRMED
    String orderId,
    String orderNumber,
    String customerId,
    String storeId,
    BigDecimal totalAmount,
    List<OrderItemPayload> items
) {
    public record OrderItemPayload(
        String productId,
        int quantity,
        BigDecimal unitPrice
    ) {}
}

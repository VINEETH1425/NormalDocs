package com.storeconnect.order.dto;

import java.math.BigDecimal;

public record OrderItemDTO(
    String id,
    String productId,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal
) {}

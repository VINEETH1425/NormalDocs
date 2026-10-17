package com.storeconnect.user.dto;

import java.math.BigDecimal;

public record CartItemDTO(
    String id,
    String productId,
    String productName,
    String productSku,
    BigDecimal unitPrice,
    int quantity,
    BigDecimal subtotal
) {}

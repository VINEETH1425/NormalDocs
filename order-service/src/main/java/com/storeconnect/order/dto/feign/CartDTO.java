package com.storeconnect.order.dto.feign;

import java.math.BigDecimal;
import java.util.List;

public record CartDTO(
    String id,
    String customerId,
    String storeId,
    List<CartItemDTO> items,
    BigDecimal totalAmount,
    int itemCount
) {
    public record CartItemDTO(
        String id,
        String productId,
        String productName,
        String productSku,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal
    ) {}
}

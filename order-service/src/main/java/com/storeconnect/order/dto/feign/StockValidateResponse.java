package com.storeconnect.order.dto.feign;

import java.util.List;

public record StockValidateResponse(
    boolean isAvailable,
    String storeId,
    List<StockIssue> issues
) {
    public record StockIssue(
        String productId,
        int requestedQuantity,
        int availableQuantity,
        String message
    ) {}
}

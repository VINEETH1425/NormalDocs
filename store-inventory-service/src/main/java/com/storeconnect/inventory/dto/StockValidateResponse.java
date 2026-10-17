package com.storeconnect.inventory.dto;

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

    public static StockValidateResponse success(String storeId) {
        return new StockValidateResponse(true, storeId, List.of());
    }

    public static StockValidateResponse failure(String storeId, List<StockIssue> issues) {
        return new StockValidateResponse(false, storeId, issues);
    }
}

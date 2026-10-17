package com.storeconnect.inventory.dto;

import java.util.List;

public record AvailabilityResponse(
    String productId,
    String productName,
    String sku,
    int totalStockAvailable,
    List<StoreAvailabilityItem> storeAvailability
) {
    public record StoreAvailabilityItem(
        String storeId,
        String storeName,
        String city,
        int quantityAvailable
    ) {}
}

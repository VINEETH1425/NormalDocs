package com.storeconnect.inventory.dto;

import java.time.LocalDateTime;

public record InventoryDTO(
    String id,
    String storeId,
    String storeName,
    String productId,
    String productName,
    String productSku,
    Integer quantityAvailable,
    Integer quantityReserved,
    Integer reorderThreshold,
    LocalDateTime updatedAt
) {}

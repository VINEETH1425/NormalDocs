package com.storeconnect.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockUpdateRequest(
    @NotNull(message = "Quantity available is required")
    @Min(value = 0, message = "Quantity available cannot be negative")
    Integer quantityAvailable,

    @Min(value = 0, message = "Reorder threshold cannot be negative")
    Integer reorderThreshold
) {}

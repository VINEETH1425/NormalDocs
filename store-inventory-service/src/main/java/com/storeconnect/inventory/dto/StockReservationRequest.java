package com.storeconnect.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record StockReservationRequest(
    @NotBlank(message = "Order ID is required")
    String orderId,

    @NotBlank(message = "Store ID is required")
    String storeId,

    @NotEmpty(message = "Items list cannot be empty")
    List<StockValidateItem> items
) {}

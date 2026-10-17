package com.storeconnect.user.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(
    @NotBlank(message = "Product ID is required")
    String productId,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity,

    String storeId // Optional: preferred store for pickup/delivery
) {}

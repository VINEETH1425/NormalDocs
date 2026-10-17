package com.storeconnect.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record StockValidateRequest(
    @NotBlank(message = "Store ID is required")
    String storeId,

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    List<StockValidateItem> items
) {}

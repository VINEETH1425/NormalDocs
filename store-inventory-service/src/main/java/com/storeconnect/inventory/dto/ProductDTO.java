package com.storeconnect.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductDTO(
    String id,
    @NotBlank(message = "SKU is required")
    String sku,
    @NotBlank(message = "Product name is required")
    String name,
    String description,
    String categoryId,
    String categoryName,
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    BigDecimal price,
    Boolean isActive
) {}

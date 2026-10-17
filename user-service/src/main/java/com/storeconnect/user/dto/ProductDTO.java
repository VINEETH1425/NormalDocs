package com.storeconnect.user.dto;

import java.math.BigDecimal;

public record ProductDTO(
    String id,
    String sku,
    String name,
    String description,
    String categoryId,
    String categoryName,
    BigDecimal price,
    Boolean isActive
) {}

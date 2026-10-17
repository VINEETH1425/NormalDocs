package com.storeconnect.user.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartDTO(
    String id,
    String customerId,
    String storeId,
    List<CartItemDTO> items,
    BigDecimal totalAmount,
    int itemCount,
    LocalDateTime updatedAt
) {}

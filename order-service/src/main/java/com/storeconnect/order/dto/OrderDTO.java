package com.storeconnect.order.dto;

import com.storeconnect.order.entity.FulfillmentType;
import com.storeconnect.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDTO(
    String id,
    String orderNumber,
    String customerId,
    String storeId,
    OrderStatus status,
    FulfillmentType fulfillmentType,
    BigDecimal totalAmount,
    List<OrderItemDTO> items,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

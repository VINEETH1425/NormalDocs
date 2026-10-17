package com.storeconnect.order.dto;

import com.storeconnect.order.entity.FulfillmentType;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
    String storeId, // Optional override: if null, uses cart's storeId or default
    @NotNull(message = "Fulfillment type is required (STORE_PICKUP or HOME_DELIVERY)")
    FulfillmentType fulfillmentType
) {}

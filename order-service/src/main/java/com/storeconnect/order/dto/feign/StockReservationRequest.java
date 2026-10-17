package com.storeconnect.order.dto.feign;

import java.util.List;

public record StockReservationRequest(
    String orderId,
    String storeId,
    List<StockValidateItem> items
) {}

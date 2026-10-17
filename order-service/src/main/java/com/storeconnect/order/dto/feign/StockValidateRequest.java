package com.storeconnect.order.dto.feign;

import java.util.List;

public record StockValidateRequest(
    String storeId,
    List<StockValidateItem> items
) {}

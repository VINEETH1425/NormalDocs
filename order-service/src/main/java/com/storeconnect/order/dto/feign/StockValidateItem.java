package com.storeconnect.order.dto.feign;

import java.util.List;

public record StockValidateItem(
    String productId,
    int quantity
) {}

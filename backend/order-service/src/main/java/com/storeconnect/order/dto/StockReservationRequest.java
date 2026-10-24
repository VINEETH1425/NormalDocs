package com.storeconnect.order.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReservationRequest {
    private String storeId;
    private String productId;
    private int quantity;
}

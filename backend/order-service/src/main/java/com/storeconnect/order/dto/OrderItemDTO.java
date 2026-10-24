package com.storeconnect.order.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {
    private String id;
    private String productId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}

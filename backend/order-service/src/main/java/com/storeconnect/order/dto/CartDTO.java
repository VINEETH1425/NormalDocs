package com.storeconnect.order.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDTO {
    private String id;
    private String customerId;
    private String storeId;
    private List<CartItemDTO> items;
    private BigDecimal totalAmount;
}

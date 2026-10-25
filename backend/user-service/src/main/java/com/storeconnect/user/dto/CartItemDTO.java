package com.storeconnect.user.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private String id;
    private String productId;
    private String productName;
    private String productSku;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal subtotal;
}

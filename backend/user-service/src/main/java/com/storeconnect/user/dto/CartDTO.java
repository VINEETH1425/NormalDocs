package com.storeconnect.user.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private String id;
    private String customerId;
    private String storeId;
    private List<CartItemDTO> items;
    private BigDecimal totalAmount;
    private int itemCount;
    private LocalDateTime updatedAt;
}

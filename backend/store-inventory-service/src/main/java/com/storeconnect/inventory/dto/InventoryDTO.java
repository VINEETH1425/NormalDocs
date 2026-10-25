package com.storeconnect.inventory.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {
    private String id;
    private String storeId;
    private String storeName;
    private String productId;
    private String productName;
    private String productSku;
    private Integer quantityAvailable;
    private Integer quantityReserved;
    private Integer reorderThreshold;
    private LocalDateTime updatedAt;
}

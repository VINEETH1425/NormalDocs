package com.storeconnect.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_inventory", schema = "inventory_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreInventory {

    @Id
    private String id;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "quantity_available", nullable = false)
    private Integer quantityAvailable = 0;

    @Column(name = "quantity_reserved", nullable = false)
    private Integer quantityReserved = 0;

    @Column(name = "reorder_threshold")
    private Integer reorderThreshold = 5;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public StoreInventory(String id, String storeId, String productId, Integer quantityAvailable, Integer quantityReserved, Integer reorderThreshold) {
        this.id = id;
        this.storeId = storeId;
        this.productId = productId;
        this.quantityAvailable = quantityAvailable != null ? quantityAvailable : 0;
        this.quantityReserved = quantityReserved != null ? quantityReserved : 0;
        this.reorderThreshold = reorderThreshold != null ? reorderThreshold : 5;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

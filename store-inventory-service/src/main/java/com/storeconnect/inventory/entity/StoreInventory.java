package com.storeconnect.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_inventory", schema = "inventory_schema",
        uniqueConstraints = @UniqueConstraint(name = "uq_store_product", columnNames = {"store_id", "product_id"}))
public class StoreInventory {

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "store_id", nullable = false, length = 50)
    private String storeId;

    @Column(name = "product_id", nullable = false, length = 50)
    private String productId;

    @Column(name = "quantity_available", nullable = false)
    private Integer quantityAvailable = 0;

    @Column(name = "quantity_reserved", nullable = false)
    private Integer quantityReserved = 0;

    @Column(name = "reorder_threshold")
    private Integer reorderThreshold = 5;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public StoreInventory() {}

    public StoreInventory(String id, String storeId, String productId, Integer quantityAvailable, Integer quantityReserved, Integer reorderThreshold, LocalDateTime updatedAt) {
        this.id = id;
        this.storeId = storeId;
        this.productId = productId;
        this.quantityAvailable = quantityAvailable != null ? quantityAvailable : 0;
        this.quantityReserved = quantityReserved != null ? quantityReserved : 0;
        this.reorderThreshold = reorderThreshold != null ? reorderThreshold : 5;
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String storeId;
        private String productId;
        private Integer quantityAvailable = 0;
        private Integer quantityReserved = 0;
        private Integer reorderThreshold = 5;
        private LocalDateTime updatedAt = LocalDateTime.now();

        public Builder id(String id) { this.id = id; return this; }
        public Builder storeId(String storeId) { this.storeId = storeId; return this; }
        public Builder productId(String productId) { this.productId = productId; return this; }
        public Builder quantityAvailable(Integer quantityAvailable) { this.quantityAvailable = quantityAvailable; return this; }
        public Builder quantityReserved(Integer quantityReserved) { this.quantityReserved = quantityReserved; return this; }
        public Builder reorderThreshold(Integer reorderThreshold) { this.reorderThreshold = reorderThreshold; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public StoreInventory build() {
            return new StoreInventory(id, storeId, productId, quantityAvailable, quantityReserved, reorderThreshold, updatedAt);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public Integer getQuantityAvailable() { return quantityAvailable; }
    public void setQuantityAvailable(Integer quantityAvailable) { this.quantityAvailable = quantityAvailable; }
    public Integer getQuantityReserved() { return quantityReserved; }
    public void setQuantityReserved(Integer quantityReserved) { this.quantityReserved = quantityReserved; }
    public Integer getReorderThreshold() { return reorderThreshold; }
    public void setReorderThreshold(Integer reorderThreshold) { this.reorderThreshold = reorderThreshold; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

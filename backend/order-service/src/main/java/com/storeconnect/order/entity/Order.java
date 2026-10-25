package com.storeconnect.order.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", schema = "order_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private String id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(nullable = false)
    private String status = "CREATED";

    @Column(name = "fulfillment_type", nullable = false)
    private String fulfillmentType = "STORE_PICKUP";

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Order(String id, String orderNumber, String customerId, String storeId, String status, String fulfillmentType, BigDecimal totalAmount) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.storeId = storeId;
        this.status = status != null ? status : "CREATED";
        this.fulfillmentType = fulfillmentType != null ? fulfillmentType : "STORE_PICKUP";
        this.totalAmount = totalAmount;
        this.items = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "CREATED";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

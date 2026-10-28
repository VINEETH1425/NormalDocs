package com.storeconnect.order.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String eventType;
    private String orderId;
    private String orderNumber;
    private String customerId;
    private String storeId;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime timestamp;
}


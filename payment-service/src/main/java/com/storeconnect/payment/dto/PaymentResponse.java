package com.storeconnect.payment.dto;

import com.storeconnect.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
    String id,
    String orderId,
    String transactionId,
    BigDecimal amount,
    PaymentStatus status,
    String failureReason,
    LocalDateTime createdAt
) {}

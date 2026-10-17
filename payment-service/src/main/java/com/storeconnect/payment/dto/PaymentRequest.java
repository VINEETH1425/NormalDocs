package com.storeconnect.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequest(
    @NotBlank(message = "Order ID is required")
    String orderId,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    BigDecimal amount,

    String paymentMethod // e.g. "CARD", "UPI", "NETBANKING"
) {}

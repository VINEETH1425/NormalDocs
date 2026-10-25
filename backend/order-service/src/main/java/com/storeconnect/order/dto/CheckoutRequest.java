package com.storeconnect.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    private String storeId;

    @NotBlank(message = "Fulfillment type is required (e.g. STORE_PICKUP)")
    private String fulfillmentType;
}

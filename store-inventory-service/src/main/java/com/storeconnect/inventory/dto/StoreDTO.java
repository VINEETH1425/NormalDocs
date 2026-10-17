package com.storeconnect.inventory.dto;

import jakarta.validation.constraints.NotBlank;

public record StoreDTO(
    String id,
    @NotBlank(message = "Store name is required")
    String name,
    @NotBlank(message = "Store code is required")
    String code,
    @NotBlank(message = "Store address is required")
    String address,
    @NotBlank(message = "City is required")
    String city,
    String phone,
    Boolean isActive
) {}

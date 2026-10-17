package com.storeconnect.user.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerUpdateRequest(
    @NotBlank(message = "First name is required")
    String firstName,

    @NotBlank(message = "Last name is required")
    String lastName,

    String phone,
    String address
) {}

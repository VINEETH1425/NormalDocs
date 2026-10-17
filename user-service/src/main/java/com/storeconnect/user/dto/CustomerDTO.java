package com.storeconnect.user.dto;

import java.time.LocalDateTime;

public record CustomerDTO(
    String id,
    String firstName,
    String lastName,
    String email,
    String phone,
    String address,
    String role,
    LocalDateTime createdAt
) {}

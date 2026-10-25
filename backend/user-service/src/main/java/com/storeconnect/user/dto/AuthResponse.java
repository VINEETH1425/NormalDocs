package com.storeconnect.user.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private long expiresInMs;
    private CustomerDTO user;
}

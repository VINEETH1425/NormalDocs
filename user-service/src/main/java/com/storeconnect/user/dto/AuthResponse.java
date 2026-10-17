package com.storeconnect.user.dto;

public record AuthResponse(
    String token,
    String tokenType,
    long expiresInMs,
    CustomerDTO user
) {
    public static AuthResponse of(String token, long expiresInMs, CustomerDTO user) {
        return new AuthResponse(token, "Bearer", expiresInMs, user);
    }
}

package com.storeconnect.user.config;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long TEST_EXPIRATION = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET, TEST_EXPIRATION);
    }

    @Test
    @DisplayName("Should generate valid JWT token with correct claims")
    void testTokenGenerationAndExtraction() {
        String userId = "USER-12345";
        String email = "aarav.sharma@example.com";
        String role = "CUSTOMER";

        String token = jwtUtil.generateToken(userId, email, role);
        assertNotNull(token);
        assertFalse(token.isBlank());

        Claims claims = jwtUtil.extractAllClaims(token);
        assertEquals(userId, claims.getSubject());
        assertEquals(email, claims.get("email"));
        assertEquals(role, claims.get("role"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }
}

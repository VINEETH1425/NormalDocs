package com.storeconnect.order.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {

    @Test
    @DisplayName("Valid transitions should be allowed by state machine")
    void testValidTransitions() {
        assertTrue(OrderStatus.CREATED.canTransitionTo(OrderStatus.CONFIRMED));
        assertTrue(OrderStatus.CREATED.canTransitionTo(OrderStatus.CANCELLED));
        assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.PROCESSING));
        assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.CANCELLED));
        assertTrue(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.READY));
        assertTrue(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.CANCELLED));
        assertTrue(OrderStatus.READY.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    @DisplayName("Illegal transitions must be rejected")
    void testIllegalTransitions() {
        assertFalse(OrderStatus.CREATED.canTransitionTo(OrderStatus.DELIVERED));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.PROCESSING));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.CONFIRMED));
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    @DisplayName("Self transitions are idempotent")
    void testSelfTransition() {
        assertTrue(OrderStatus.CREATED.canTransitionTo(OrderStatus.CREATED));
        assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.CONFIRMED));
        assertTrue(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.PROCESSING));
    }
}

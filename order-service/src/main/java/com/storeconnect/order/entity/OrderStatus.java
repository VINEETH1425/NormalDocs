package com.storeconnect.order.entity;

public enum OrderStatus {
    CREATED,
    CONFIRMED,
    PROCESSING,
    READY,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus next) {
        if (this == next) return true;
        return switch (this) {
            case CREATED -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == PROCESSING || next == CANCELLED;
            case PROCESSING -> next == READY || next == CANCELLED;
            case READY -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}

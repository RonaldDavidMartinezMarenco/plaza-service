package com.foodcourt.plaza.domain.model;

import java.util.List;

import com.foodcourt.plaza.domain.exception.InvalidOrderStatus;

/**
 * Order states. A new order is {@link #PENDING}.
 * {@link #DELIVERED} and {@link #CANCELLED} leave the customer free to order again.
 */
public enum OrderStatus {
    PENDING,
    IN_PREPARATION,
    READY,
    DELIVERED,
    CANCELLED;

    /** Statuses that occupy the one-active-order rule. */
    public static List<OrderStatus> inProgress() {
        return List.of(PENDING, IN_PREPARATION, READY);
    }

    /** The query may carry the status once. Zero or several values are rejected. */
    public static void requireOne(String[] raw) {
        if (raw == null || raw.length != 1) {
            throw new InvalidOrderStatus();
        }
    }

    /** The stored name, with the same capitals. The text is not trimmed. Anything else is rejected. */
    public static OrderStatus parseExact(String raw) {
        if (raw == null) {
            throw new InvalidOrderStatus();
        }
        try {
            return OrderStatus.valueOf(raw);
        } catch (IllegalArgumentException failure) {
            throw new InvalidOrderStatus();
        }
    }
}

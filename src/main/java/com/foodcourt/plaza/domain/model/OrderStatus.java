package com.foodcourt.plaza.domain.model;

import java.util.List;

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
}

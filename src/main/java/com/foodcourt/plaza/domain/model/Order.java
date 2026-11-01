package com.foodcourt.plaza.domain.model;

import java.util.List;

/**
 * Persisted order. {@code id} is null until PostgreSQL assigns it.
 * {@link #place} always starts at {@link OrderStatus#PENDING}, with no employee and no PIN.
 * {@code pinHash} stays null here. A later story fills it and it is never sent to the client.
 */
public record Order(
        Long id,
        long customerId,
        long restaurantId,
        OrderStatus status,
        Long assignedEmployeeId,
        String pinHash,
        List<OrderLine> lines) {

    public Order {
        lines = List.copyOf(lines);
    }

    public static Order place(long customerId, long restaurantId, List<OrderLine> lines) {
        return new Order(null, customerId, restaurantId, OrderStatus.PENDING, null, null, lines);
    }
}

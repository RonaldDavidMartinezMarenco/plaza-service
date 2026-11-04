package com.foodcourt.plaza.domain.model;

import java.util.List;

import com.foodcourt.plaza.domain.exception.OrderAlreadyAssigned;
import com.foodcourt.plaza.domain.exception.OrderNotAssignable;

/**
 * Persisted order. {@code id} is null until PostgreSQL assigns it.
 * {@link #place} always starts at {@link OrderStatus#PENDING}, with no employee and no PIN.
 * {@link #assign} is the only way into {@link OrderStatus#IN_PREPARATION}.
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

    /**
     * The employee takes this pending order. Customer, restaurant, lines, and PIN stay as stored.
     */
    public Order assign(long employeeId) {
        requirePending();
        requireUnassigned();
        return new Order(id, customerId, restaurantId, OrderStatus.IN_PREPARATION, employeeId, pinHash, lines);
    }

    private void requirePending() {
        if (status != OrderStatus.PENDING) {
            throw new OrderNotAssignable(status);
        }
    }

    private void requireUnassigned() {
        if (assignedEmployeeId != null) {
            throw new OrderAlreadyAssigned();
        }
    }
}

package com.foodcourt.plaza.domain.model;

import java.util.List;

import com.foodcourt.plaza.domain.exception.OrderAlreadyAssigned;
import com.foodcourt.plaza.domain.exception.OrderAssignedToAnotherEmployee;
import com.foodcourt.plaza.domain.exception.OrderNotAssignable;
import com.foodcourt.plaza.domain.exception.OrderNotReady;

/**
 * Persisted order. {@code id} is null until PostgreSQL assigns it.
 * {@link #place} always starts at {@link OrderStatus#PENDING}, with no employee and no PIN.
 * {@link #assign} is the only way into {@link OrderStatus#IN_PREPARATION}.
 * {@link #markReady} is the only way into {@link OrderStatus#READY}. {@code pinHash} stays null until {@link #withPin}.
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

    /**
     * The assigned employee finishes preparation. The PIN hash is attached afterwards with {@link #withPin}.
     */
    public Order markReady(long employeeId) {
        requireInPreparation();
        requireAssignee(employeeId);
        requireWithoutPin();
        return new Order(id, customerId, restaurantId, OrderStatus.READY, assignedEmployeeId, null, lines);
    }

    /** Attaches the bcrypt hash. The clear PIN is not stored on the order. */
    public Order withPin(String pinHash) {
        return new Order(id, customerId, restaurantId, status, assignedEmployeeId, pinHash, lines);
    }

    private void requireInPreparation() {
        if (status != OrderStatus.IN_PREPARATION) {
            throw new OrderNotReady(status);
        }
    }

    private void requireAssignee(long employeeId) {
        if (assignedEmployeeId == null || assignedEmployeeId != employeeId) {
            throw new OrderAssignedToAnotherEmployee();
        }
    }

    private void requireWithoutPin() {
        if (pinHash != null) {
            throw new OrderNotReady(status);
        }
    }
}

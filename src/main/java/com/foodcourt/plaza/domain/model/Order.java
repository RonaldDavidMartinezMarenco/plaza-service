package com.foodcourt.plaza.domain.model;

import java.util.List;

import com.foodcourt.plaza.domain.exception.OrderAlreadyAssigned;
import com.foodcourt.plaza.domain.exception.OrderAssignedToAnotherEmployee;
import com.foodcourt.plaza.domain.exception.OrderNotAssignable;
import com.foodcourt.plaza.domain.exception.OrderNotCancellable;
import com.foodcourt.plaza.domain.exception.OrderNotDeliverable;
import com.foodcourt.plaza.domain.exception.OrderNotReady;

/**
 * Persisted order. {@code id} is null until PostgreSQL assigns it.
 * {@link #place} always starts at {@link OrderStatus#PENDING}, with no employee and no PIN.
 * {@link #assign} is the only way into {@link OrderStatus#IN_PREPARATION}.
 * {@link #markReady} is the only way into {@link OrderStatus#READY}. {@code pinHash} stays null until {@link #withPin}.
 * {@link #deliver} is the only way into {@link OrderStatus#DELIVERED}. That status does not change again.
 * {@link #cancel} is the only way into {@link OrderStatus#CANCELLED}, and only from {@link OrderStatus#PENDING}.
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

    /**
     * The assigned employee closes a ready order. The hash stays; the clear PIN is not stored.
     * {@link OrderStatus#DELIVERED} has no further transition.
     */
    public Order deliver(long employeeId) {
        requireReady();
        requireAssignee(employeeId);
        requirePinStored();
        return new Order(id, customerId, restaurantId, OrderStatus.DELIVERED, assignedEmployeeId, pinHash, lines);
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

    private void requireReady() {
        if (status != OrderStatus.READY) {
            throw new OrderNotDeliverable(status);
        }
    }

    private void requirePinStored() {
        if (pinHash == null || pinHash.isBlank()) {
            throw new OrderNotDeliverable(status);
        }
    }

    /**
     * The customer withdraws this pending order. Restaurant, lines, employee, and PIN stay as stored.
     */
    public Order cancel() {
        requireCancellable();
        return new Order(id, customerId, restaurantId, OrderStatus.CANCELLED, assignedEmployeeId, pinHash, lines);
    }

    private void requireCancellable() {
        if (status != OrderStatus.PENDING) {
            throw new OrderNotCancellable();
        }
    }
}

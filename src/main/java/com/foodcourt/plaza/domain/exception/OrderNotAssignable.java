package com.foodcourt.plaza.domain.exception;

import com.foodcourt.plaza.domain.model.OrderStatus;

/**
 * The order is not {@link OrderStatus#PENDING}, so it cannot move to preparation.
 * Mapped to HTTP 409.
 */
public final class OrderNotAssignable extends RuntimeException {

    public OrderNotAssignable(OrderStatus status) {
        super("The order is " + status + " and cannot be assigned");
    }
}

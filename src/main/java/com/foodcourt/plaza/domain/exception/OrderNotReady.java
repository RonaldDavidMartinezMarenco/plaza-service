package com.foodcourt.plaza.domain.exception;

import com.foodcourt.plaza.domain.model.OrderStatus;

/**
 * The order is not {@link OrderStatus#IN_PREPARATION}, so it cannot become ready.
 * Mapped to HTTP 409.
 */
public final class OrderNotReady extends RuntimeException {

    public OrderNotReady(OrderStatus status) {
        super("The order is " + status + " and cannot be marked ready");
    }
}

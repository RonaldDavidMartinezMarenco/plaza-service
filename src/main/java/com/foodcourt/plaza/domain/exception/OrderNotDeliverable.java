package com.foodcourt.plaza.domain.exception;

import com.foodcourt.plaza.domain.model.OrderStatus;

/**
 * The order is not a ready order that still has its PIN hash, so it cannot be delivered.
 * Mapped to HTTP 409. {@link OrderStatus#DELIVERED} is included: that status does not change.
 */
public final class OrderNotDeliverable extends RuntimeException {

    public OrderNotDeliverable(OrderStatus status) {
        super("The order is " + status + " and cannot be delivered");
    }
}

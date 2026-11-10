package com.foodcourt.plaza.domain.exception;

import com.foodcourt.plaza.domain.model.OrderStatus;

/**
 * The order cannot move to {@link OrderStatus#CANCELLED}. Mapped to HTTP 409.
 * Every status other than {@link OrderStatus#PENDING} uses the backlog sentence.
 */
public final class OrderNotCancellable extends RuntimeException {

    public OrderNotCancellable() {
        super("Lo sentimos, tu pedido ya está en preparación y no puede cancelarse");
    }

    /** The conditional update changed no row and a new read still shows the order as pending. */
    private OrderNotCancellable(String detail) {
        super(detail);
    }

    public static OrderNotCancellable unchanged() {
        return new OrderNotCancellable("The order could not be cancelled");
    }
}

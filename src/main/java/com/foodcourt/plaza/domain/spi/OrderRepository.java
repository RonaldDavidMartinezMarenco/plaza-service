package com.foodcourt.plaza.domain.spi;

import com.foodcourt.plaza.domain.model.Order;

/**
 * Persistence port for {@code customer_order} and its lines.
 * {@link #hasActive} is true when the customer already has an order in progress.
 */
public interface OrderRepository {

    boolean hasActive(long customerId);

    Order save(Order order);
}

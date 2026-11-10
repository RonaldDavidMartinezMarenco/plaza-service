package com.foodcourt.plaza.domain.spi;

import java.util.Optional;

import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderListQuery;
import com.foodcourt.plaza.domain.model.PageResult;

/**
 * Persistence port for {@code customer_order} and its lines.
 * {@link #hasActive} is true when the customer already has an order in progress.
 * {@link #findPage} returns one restaurant and one status, ordered by id.
 * {@link #findByIdAndRestaurantId} hides an order that belongs to another restaurant.
 * {@link #assignIfPending} writes the employee and {@code IN_PREPARATION} only while the row is still free.
 * {@link #markReadyIfInPreparation} writes {@code READY} and the PIN hash only for the assigned employee.
 * {@link #revertReady} clears that hash and returns the row to {@code IN_PREPARATION} when the SMS is rejected.
 */
public interface OrderRepository {

    boolean hasActive(long customerId);

    Order save(Order order);

    PageResult<Order> findPage(long restaurantId, OrderListQuery query);

    Optional<Order> findByIdAndRestaurantId(long orderId, long restaurantId);

    boolean assignIfPending(long orderId, long restaurantId, long employeeId);

    boolean markReadyIfInPreparation(long orderId, long restaurantId, long employeeId, String pinHash);

    boolean revertReady(long orderId, long restaurantId, long employeeId, String pinHash);
}

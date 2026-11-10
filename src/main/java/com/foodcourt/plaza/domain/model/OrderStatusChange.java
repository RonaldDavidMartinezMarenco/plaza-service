package com.foodcourt.plaza.domain.model;

/**
 * One status change to record in traceability. The other service stamps the time.
 * {@code employeeId} is null when the order is placed or cancelled.
 */
public record OrderStatusChange(
        long orderId,
        long customerId,
        long restaurantId,
        Long employeeId,
        OrderStatus previousStatus,
        OrderStatus newStatus) {
}

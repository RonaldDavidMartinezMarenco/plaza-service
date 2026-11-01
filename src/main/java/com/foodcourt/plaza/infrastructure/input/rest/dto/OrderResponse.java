package com.foodcourt.plaza.infrastructure.input.rest.dto;

import java.util.List;

import com.foodcourt.plaza.domain.model.OrderStatus;

/**
 * Stored order. {@code pinHash} is omitted. {@code assignedEmployeeId} is null until an employee takes it.
 */
public record OrderResponse(
        Long id,
        long customerId,
        long restaurantId,
        OrderStatus status,
        Long assignedEmployeeId,
        List<OrderLineResponse> lines) {
}

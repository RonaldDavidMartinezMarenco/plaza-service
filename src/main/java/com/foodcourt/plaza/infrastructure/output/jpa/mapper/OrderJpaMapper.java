package com.foodcourt.plaza.infrastructure.output.jpa.mapper;

import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.infrastructure.output.jpa.entity.CustomerOrderEntity;
import com.foodcourt.plaza.infrastructure.output.jpa.entity.OrderLineEntity;

/** Copies fields between the order rows and {@link Order}. Applies no rule. */
public final class OrderJpaMapper {

    public CustomerOrderEntity toEntity(Order order) {
        CustomerOrderEntity entity = new CustomerOrderEntity(
                order.id(),
                order.customerId(),
                order.restaurantId(),
                order.status(),
                order.assignedEmployeeId(),
                order.pinHash());
        addLines(entity, order);
        return entity;
    }

    public Order toDomain(CustomerOrderEntity entity) {
        return new Order(
                entity.getId(),
                entity.getCustomerId(),
                entity.getRestaurantId(),
                entity.getStatus(),
                entity.getAssignedEmployeeId(),
                entity.getPinHash(),
                entity.getLines().stream().map(this::toLine).toList());
    }

    private static void addLines(CustomerOrderEntity entity, Order order) {
        for (OrderLine line : order.lines()) {
            entity.addLine(new OrderLineEntity(null, line.dishId(), line.quantity()));
        }
    }

    private OrderLine toLine(OrderLineEntity entity) {
        return new OrderLine(entity.getDishId(), entity.getQuantity());
    }
}

package com.foodcourt.plaza.infrastructure.output.jpa.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.model.PageResult;
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

    public PageResult<Order> toPage(Page<CustomerOrderEntity> page, List<CustomerOrderEntity> withLines) {
        Map<Long, CustomerOrderEntity> byId = index(withLines);
        List<Order> items = page.getContent().stream().map(entity -> toDomain(loaded(byId, entity))).toList();
        return new PageResult<>(items, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    private OrderLine toLine(OrderLineEntity entity) {
        return new OrderLine(entity.getDishId(), entity.getQuantity());
    }

    private static Map<Long, CustomerOrderEntity> index(List<CustomerOrderEntity> withLines) {
        Map<Long, CustomerOrderEntity> byId = new HashMap<>();
        for (CustomerOrderEntity entity : withLines) {
            byId.put(entity.getId(), entity);
        }
        return byId;
    }

    private static CustomerOrderEntity loaded(Map<Long, CustomerOrderEntity> byId, CustomerOrderEntity entity) {
        CustomerOrderEntity loaded = byId.get(entity.getId());
        if (loaded == null) {
            return entity;
        }
        return loaded;
    }
}

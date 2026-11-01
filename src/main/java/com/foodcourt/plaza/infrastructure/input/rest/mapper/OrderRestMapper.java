package com.foodcourt.plaza.infrastructure.input.rest.mapper;

import java.util.List;

import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.model.PlaceOrderDraft;
import com.foodcourt.plaza.domain.model.PlaceOrderDraft.OrderLineDraft;
import com.foodcourt.plaza.infrastructure.input.rest.dto.OrderLineRequest;
import com.foodcourt.plaza.infrastructure.input.rest.dto.OrderLineResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.OrderResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.PlaceOrderRequest;

/** Copies fields between the HTTP contract and the domain. Applies no rule. */
public final class OrderRestMapper {

    public PlaceOrderDraft toDraft(PlaceOrderRequest request) {
        return new PlaceOrderDraft(request.restaurantId(), lines(request.lines()));
    }

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.id(),
                order.customerId(),
                order.restaurantId(),
                order.status(),
                order.assignedEmployeeId(),
                order.lines().stream().map(this::toResponse).toList());
    }

    private static List<OrderLineDraft> lines(List<OrderLineRequest> lines) {
        return lines.stream().map(OrderRestMapper::toDraft).toList();
    }

    private static OrderLineDraft toDraft(OrderLineRequest line) {
        return new OrderLineDraft(line.dishId(), line.quantity());
    }

    private OrderLineResponse toResponse(OrderLine line) {
        return new OrderLineResponse(line.dishId(), line.quantity());
    }
}

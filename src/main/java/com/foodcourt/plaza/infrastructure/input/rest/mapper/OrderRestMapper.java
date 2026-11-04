package com.foodcourt.plaza.infrastructure.input.rest.mapper;

import java.util.List;

import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.model.OrderListQuery;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.model.PlaceOrderDraft;
import com.foodcourt.plaza.domain.model.PlaceOrderDraft.OrderLineDraft;
import com.foodcourt.plaza.infrastructure.input.rest.dto.OrderLineRequest;
import com.foodcourt.plaza.infrastructure.input.rest.dto.OrderLineResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.OrderResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.PageResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.PlaceOrderRequest;

/** Copies fields between the HTTP contract and the domain. Applies no rule. */
public final class OrderRestMapper {

    public PlaceOrderDraft toDraft(PlaceOrderRequest request) {
        return new PlaceOrderDraft(request.restaurantId(), lines(request.lines()));
    }

    public OrderListQuery toListQuery(OrderStatus status, int page, int size) {
        return new OrderListQuery(new PageQuery(page, size), status);
    }

    public PageResponse<OrderResponse> toPage(PageResult<Order> page) {
        List<OrderResponse> items = page.items().stream().map(this::toResponse).toList();
        return new PageResponse<>(items, page.page(), page.size(), page.totalItems(), page.totalPages());
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

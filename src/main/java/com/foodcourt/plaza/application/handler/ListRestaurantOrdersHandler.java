package com.foodcourt.plaza.application.handler;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.ListRestaurantOrders;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderListQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

/**
 * Entry the controller calls. The rules stay in {@link ListRestaurantOrders}.
 * This type reads the caller and forwards the call.
 */
@Service
public final class ListRestaurantOrdersHandler {

    private final ListRestaurantOrders listRestaurantOrders;
    private final CurrentCaller currentCaller;

    public ListRestaurantOrdersHandler(ListRestaurantOrders listRestaurantOrders, CurrentCaller currentCaller) {
        this.listRestaurantOrders = listRestaurantOrders;
        this.currentCaller = currentCaller;
    }

    public PageResult<Order> list(OrderListQuery query) {
        return listRestaurantOrders.list(currentCaller.require(), query);
    }
}

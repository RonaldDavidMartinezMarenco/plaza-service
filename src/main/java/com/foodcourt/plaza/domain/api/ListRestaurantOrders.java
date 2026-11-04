package com.foodcourt.plaza.domain.api;

import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderListQuery;
import com.foodcourt.plaza.domain.model.PageResult;

/** Inbound port for listing one restaurant's orders by status. The caller must be an employee of that restaurant. */
public interface ListRestaurantOrders {

    PageResult<Order> list(Caller caller, OrderListQuery query);
}

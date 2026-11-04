package com.foodcourt.plaza.domain.api;

import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;

/**
 * Inbound port for an employee to take a pending order of their restaurant
 * and move it to {@code IN_PREPARATION}.
 */
public interface AssignOrder {

    Order assign(Caller caller, long orderId);
}

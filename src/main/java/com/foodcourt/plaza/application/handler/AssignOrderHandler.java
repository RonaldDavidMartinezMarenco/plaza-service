package com.foodcourt.plaza.application.handler;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.AssignOrder;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

/**
 * Entry the controller calls. The rules stay in {@link AssignOrder}.
 * This type reads the caller and forwards the order id.
 */
@Service
public final class AssignOrderHandler {

    private final AssignOrder assignOrder;
    private final CurrentCaller currentCaller;

    public AssignOrderHandler(AssignOrder assignOrder, CurrentCaller currentCaller) {
        this.assignOrder = assignOrder;
        this.currentCaller = currentCaller;
    }

    public Order assign(long orderId) {
        return assignOrder.assign(currentCaller.require(), orderId);
    }
}

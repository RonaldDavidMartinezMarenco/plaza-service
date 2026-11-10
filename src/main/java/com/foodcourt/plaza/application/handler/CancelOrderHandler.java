package com.foodcourt.plaza.application.handler;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.CancelOrder;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

/**
 * Entry the controller calls. The rules stay in {@link CancelOrder}.
 * This type reads the caller and forwards the order id.
 */
@Service
public final class CancelOrderHandler {

    private final CancelOrder cancelOrder;
    private final CurrentCaller currentCaller;

    public CancelOrderHandler(CancelOrder cancelOrder, CurrentCaller currentCaller) {
        this.cancelOrder = cancelOrder;
        this.currentCaller = currentCaller;
    }

    public Order cancel(long orderId) {
        return cancelOrder.cancel(currentCaller.require(), orderId);
    }
}

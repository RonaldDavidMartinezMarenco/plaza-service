package com.foodcourt.plaza.application.handler;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.DeliverOrder;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

/**
 * Entry the controller calls. The rules stay in {@link DeliverOrder}.
 * This type reads the caller and forwards the order id and the PIN.
 */
@Service
public final class DeliverOrderHandler {

    private final DeliverOrder deliverOrder;
    private final CurrentCaller currentCaller;

    public DeliverOrderHandler(DeliverOrder deliverOrder, CurrentCaller currentCaller) {
        this.deliverOrder = deliverOrder;
        this.currentCaller = currentCaller;
    }

    public Order deliver(long orderId, String pin) {
        return deliverOrder.deliver(currentCaller.require(), orderId, pin);
    }
}

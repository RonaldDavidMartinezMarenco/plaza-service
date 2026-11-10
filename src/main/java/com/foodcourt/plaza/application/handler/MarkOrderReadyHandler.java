package com.foodcourt.plaza.application.handler;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.MarkOrderReady;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

/**
 * Entry the controller calls. The rules stay in {@link MarkOrderReady}.
 * This type reads the caller and forwards the order id.
 */
@Service
public final class MarkOrderReadyHandler {

    private final MarkOrderReady markOrderReady;
    private final CurrentCaller currentCaller;

    public MarkOrderReadyHandler(MarkOrderReady markOrderReady, CurrentCaller currentCaller) {
        this.markOrderReady = markOrderReady;
        this.currentCaller = currentCaller;
    }

    public Order markReady(long orderId) {
        return markOrderReady.markReady(currentCaller.require(), orderId);
    }
}

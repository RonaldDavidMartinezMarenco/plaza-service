package com.foodcourt.plaza.application.handler;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.PlaceOrder;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.PlaceOrderDraft;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

/**
 * Entry the controller calls. Validation stays on the request DTO and the
 * rules stay in {@link PlaceOrder}. This type reads the caller and forwards the call.
 */
@Service
public final class PlaceOrderHandler {

    private final PlaceOrder placeOrder;
    private final CurrentCaller currentCaller;

    public PlaceOrderHandler(PlaceOrder placeOrder, CurrentCaller currentCaller) {
        this.placeOrder = placeOrder;
        this.currentCaller = currentCaller;
    }

    public Order place(PlaceOrderDraft draft) {
        return placeOrder.place(currentCaller.require(), draft);
    }
}

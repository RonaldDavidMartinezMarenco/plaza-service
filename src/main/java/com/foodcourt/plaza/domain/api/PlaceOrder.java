package com.foodcourt.plaza.domain.api;

import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.PlaceOrderDraft;

/** Inbound port for placing an order. The caller is the customer. */
public interface PlaceOrder {

    Order place(Caller caller, PlaceOrderDraft draft);
}

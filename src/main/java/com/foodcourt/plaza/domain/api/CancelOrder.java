package com.foodcourt.plaza.domain.api;

import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;

/** Inbound port that cancels a customer's own pending order. */
public interface CancelOrder {

    Order cancel(Caller caller, long orderId);
}

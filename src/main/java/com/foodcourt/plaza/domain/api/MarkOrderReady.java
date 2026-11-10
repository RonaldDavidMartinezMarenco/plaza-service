package com.foodcourt.plaza.domain.api;

import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;

/** Inbound port that marks an order ready and asks for the PIN to be sent. */
public interface MarkOrderReady {

    Order markReady(Caller caller, long orderId);
}

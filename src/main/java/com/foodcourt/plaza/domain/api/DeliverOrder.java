package com.foodcourt.plaza.domain.api;

import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;

/** Inbound port that delivers a ready order when the security PIN matches. */
public interface DeliverOrder {

    Order deliver(Caller caller, long orderId, String pin);
}

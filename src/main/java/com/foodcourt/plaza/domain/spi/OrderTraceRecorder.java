package com.foodcourt.plaza.domain.spi;

import com.foodcourt.plaza.domain.model.OrderStatusChange;

/** Sends one status change to traceability-service. */
public interface OrderTraceRecorder {

    void record(OrderStatusChange change);
}

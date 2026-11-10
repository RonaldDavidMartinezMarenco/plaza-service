package com.foodcourt.plaza.application.usecase;

import java.util.function.BooleanSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foodcourt.plaza.domain.exception.TraceabilityRejected;
import com.foodcourt.plaza.domain.exception.TraceabilityUnavailable;
import com.foodcourt.plaza.domain.model.OrderStatusChange;
import com.foodcourt.plaza.domain.spi.OrderTraceRecorder;

/**
 * Records a status change after plaza has stored it.
 * A rejection undoes that write. A failed undo is logged and the rejection still propagates.
 */
final class OrderTraceRule {

    private static final Logger log = LoggerFactory.getLogger(OrderTraceRule.class);

    private final OrderTraceRecorder traces;

    OrderTraceRule(OrderTraceRecorder traces) {
        this.traces = traces;
    }

    void record(OrderStatusChange change, BooleanSupplier undo) {
        try {
            traces.record(change);
        } catch (TraceabilityRejected | TraceabilityUnavailable failure) {
            report(change, undo.getAsBoolean());
            throw failure;
        }
    }

    private static void report(OrderStatusChange change, boolean undone) {
        if (!undone) {
            log.error("Order {} stayed {} after the trace was rejected", change.orderId(), change.newStatus());
        }
    }
}

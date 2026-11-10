package com.foodcourt.plaza.domain.exception;

/** traceability-service rejected the status change. Mapped to HTTP 409. */
public final class TraceabilityRejected extends RuntimeException {

    public TraceabilityRejected() {
        super("The status change was rejected");
    }
}

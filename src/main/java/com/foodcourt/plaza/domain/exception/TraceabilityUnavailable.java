package com.foodcourt.plaza.domain.exception;

/** traceability-service did not accept the status change. Mapped to HTTP 503. */
public final class TraceabilityUnavailable extends RuntimeException {

    public TraceabilityUnavailable() {
        super("The traceability service is unavailable");
    }
}

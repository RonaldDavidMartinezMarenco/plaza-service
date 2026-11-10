package com.foodcourt.plaza.domain.exception;

/** The messaging service did not answer. Mapped to HTTP 503. */
public final class NotificationUnavailable extends RuntimeException {

    public NotificationUnavailable() {
        super("The messaging service is unavailable");
    }
}

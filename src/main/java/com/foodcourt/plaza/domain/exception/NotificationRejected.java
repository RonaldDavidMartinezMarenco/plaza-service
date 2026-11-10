package com.foodcourt.plaza.domain.exception;

/** The messaging service rejected the SMS. Mapped to HTTP 502. The message does not include the PIN. */
public final class NotificationRejected extends RuntimeException {

    public NotificationRejected() {
        super("The order-ready message was rejected");
    }
}

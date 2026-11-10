package com.foodcourt.plaza.domain.spi;

/**
 * Asks messaging-service to send the ready-order PIN.
 * A rejection leaves the caller responsible for undoing the status change.
 */
public interface OrderReadyNotifier {

    void notifyReady(long orderId, String restaurantName, String phone, String pin);
}

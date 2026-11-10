package com.foodcourt.plaza.domain.spi;

/** Reads a customer's phone from user-service. */
public interface CustomerPhoneDirectory {

    String findPhone(long customerId);
}

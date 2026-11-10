package com.foodcourt.plaza.domain.spi;

/** Stores a PIN as a bcrypt hash. The clear value is not returned. */
public interface PinHasher {

    String hash(String pin);
}

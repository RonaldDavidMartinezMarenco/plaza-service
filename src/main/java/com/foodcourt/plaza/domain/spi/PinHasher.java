package com.foodcourt.plaza.domain.spi;

/** Stores a PIN as a bcrypt hash and checks a later value against that hash. */
public interface PinHasher {

    String hash(String pin);

    boolean matches(String pin, String hash);
}

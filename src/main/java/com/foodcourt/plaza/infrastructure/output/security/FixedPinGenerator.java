package com.foodcourt.plaza.infrastructure.output.security;

import com.foodcourt.plaza.domain.model.SecurityPin;
import com.foodcourt.plaza.domain.spi.PinGenerator;

/**
 * Returns one configured PIN. Used so a local seed can deliver an order.
 * The value is still sent by SMS and stored only as a bcrypt hash.
 */
public final class FixedPinGenerator implements PinGenerator {

    private final String pin;

    public FixedPinGenerator(String pin) {
        this.pin = new SecurityPin(pin).value();
    }

    @Override
    public String generate() {
        return pin;
    }
}

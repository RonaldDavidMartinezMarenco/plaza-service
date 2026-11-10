package com.foodcourt.plaza.infrastructure.output.security;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.model.SecurityPin;
import com.foodcourt.plaza.domain.spi.PinGenerator;

/** Draws a six-digit PIN, including leading zeros. */
@Service
public final class SecurePinGenerator implements PinGenerator {

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {
        int value = random.nextInt(1_000_000);
        return String.format("%0" + SecurityPin.LENGTH + "d", value);
    }
}

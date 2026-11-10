package com.foodcourt.plaza.infrastructure.output.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.spi.PinHasher;

/** Stores the delivery PIN with bcrypt. The clear PIN is not kept. */
@Service
public final class BcryptPinHasher implements PinHasher {

    private final PasswordEncoder encoder;

    public BcryptPinHasher(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String hash(String pin) {
        return encoder.encode(pin);
    }
}

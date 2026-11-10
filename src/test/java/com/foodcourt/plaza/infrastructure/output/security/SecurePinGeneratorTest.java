package com.foodcourt.plaza.infrastructure.output.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.foodcourt.plaza.domain.model.SecurityPin;

class SecurePinGeneratorTest {

    @Test
    void generateReturnsSixDigits() {
        String pin = new SecurePinGenerator().generate();

        assertThat(pin).hasSize(SecurityPin.LENGTH);
        assertThat(new SecurityPin(pin).value()).isEqualTo(pin);
    }
}

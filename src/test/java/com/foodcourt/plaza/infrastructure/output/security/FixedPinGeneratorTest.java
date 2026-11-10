package com.foodcourt.plaza.infrastructure.output.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class FixedPinGeneratorTest {

    @Test
    void generateReturnsTheConfiguredPin() {
        assertThat(new FixedPinGenerator("048215").generate()).isEqualTo("048215");
    }

    @Test
    void rejectsAPinThatIsNotSixDigits() {
        assertThatThrownBy(() -> new FixedPinGenerator("12345"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A security PIN is 6 digits");
    }
}

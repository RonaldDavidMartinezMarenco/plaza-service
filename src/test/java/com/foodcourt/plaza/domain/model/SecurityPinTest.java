package com.foodcourt.plaza.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SecurityPinTest {

    @Test
    void acceptsSixDigitsIncludingALeadingZero() {
        assertThat(new SecurityPin("048215").value()).isEqualTo("048215");
    }

    @Test
    void rejectsAPinThatIsNotSixDigits() {
        assertThatThrownBy(() -> new SecurityPin("48215"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A security PIN is 6 digits");
    }

    @Test
    void rejectsAMissingPin() {
        assertThatThrownBy(() -> new SecurityPin(null)).isInstanceOf(IllegalArgumentException.class);
    }
}

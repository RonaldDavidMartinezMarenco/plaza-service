package com.foodcourt.plaza.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.foodcourt.plaza.infrastructure.output.security.FixedPinGenerator;
import com.foodcourt.plaza.infrastructure.output.security.SecurePinGenerator;

class PinGeneratorConfigTest {

    @Test
    void selectKeepsTheRandomGeneratorWhenThePropertyIsBlank() {
        assertThat(PinGeneratorConfig.select(null)).isInstanceOf(SecurePinGenerator.class);
        assertThat(PinGeneratorConfig.select("")).isInstanceOf(SecurePinGenerator.class);
        assertThat(PinGeneratorConfig.select("   ")).isInstanceOf(SecurePinGenerator.class);
    }

    @Test
    void selectUsesTheFixedPinWhenItHasSixDigits() {
        assertThat(PinGeneratorConfig.select("048215")).isInstanceOf(FixedPinGenerator.class);
        assertThat(PinGeneratorConfig.select("048215").generate()).isEqualTo("048215");
    }

    @Test
    void selectRejectsAPinThatIsNotSixDigits() {
        assertThatThrownBy(() -> PinGeneratorConfig.select("abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A security PIN is 6 digits");
    }
}

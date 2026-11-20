package com.foodcourt.plaza.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.foodcourt.plaza.domain.spi.PinGenerator;
import com.foodcourt.plaza.infrastructure.output.security.FixedPinGenerator;
import com.foodcourt.plaza.infrastructure.output.security.SecurePinGenerator;

class PinGeneratorConfigTest {

    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(PinGeneratorConfig.class);

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

    @Test
    void readsSeedPinWhenTheApplicationPropertyIsAbsent() {
        context.withPropertyValues("SEED_PIN=048215")
                .run(application -> assertThat(application.getBean(PinGenerator.class).generate())
                        .isEqualTo("048215"));
    }

    @Test
    void explicitApplicationPropertyTakesPrecedenceOverSeedPin() {
        context.withPropertyValues("app.pin.fixed=", "SEED_PIN=048215")
                .run(application -> assertThat(application.getBean(PinGenerator.class))
                        .isInstanceOf(SecurePinGenerator.class));
    }
}

package com.foodcourt.plaza.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.foodcourt.plaza.domain.spi.PinGenerator;
import com.foodcourt.plaza.infrastructure.output.security.FixedPinGenerator;
import com.foodcourt.plaza.infrastructure.output.security.SecurePinGenerator;

/** One PIN generator. A blank {@code app.pin.fixed} keeps the random generator. */
@Configuration
public class PinGeneratorConfig {

    @Bean
    PinGenerator pinGenerator(@Value("${app.pin.fixed:}") String fixed) {
        return select(fixed);
    }

    static PinGenerator select(String fixed) {
        if (fixed == null || fixed.isBlank()) {
            return new SecurePinGenerator();
        }
        return new FixedPinGenerator(fixed);
    }
}

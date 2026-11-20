package com.foodcourt.plaza.infrastructure.output.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import com.foodcourt.plaza.domain.model.SecurityPin;

class SecurePinGeneratorTest {

    @Test
    void generateReturnsSixDigits() {
        String pin = new SecurePinGenerator().generate();

        assertThat(pin).hasSize(SecurityPin.LENGTH);
        assertThat(new SecurityPin(pin).value()).isEqualTo(pin);
    }

    @Test
    @ResourceLock(Resources.LOCALE)
    void generateUsesAsciiDigitsWithAnArabicFormattingLocale() {
        Locale original = Locale.getDefault(Locale.Category.FORMAT);
        try {
            Locale.setDefault(Locale.Category.FORMAT, Locale.forLanguageTag("ar-EG-u-nu-arab"));

            assertThat(new SecurePinGenerator().generate()).matches("[0-9]{6}");
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, original);
        }
    }
}

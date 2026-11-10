package com.foodcourt.plaza.infrastructure.output.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BcryptPinHasherTest {

    @Test
    void matchesAcceptsTheOriginalPinAndRejectsAnother() {
        BcryptPinHasher hasher = new BcryptPinHasher(new BCryptPasswordEncoder());
        String hash = hasher.hash("048215");

        assertThat(hash).isNotEqualTo("048215");
        assertThat(hasher.hash("048215")).isNotEqualTo(hash);
        assertThat(hasher.matches("048215", hash)).isTrue();
        assertThat(hasher.matches("048216", hash)).isFalse();
    }
}

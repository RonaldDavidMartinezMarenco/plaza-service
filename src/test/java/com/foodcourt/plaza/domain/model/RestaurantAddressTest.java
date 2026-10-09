package com.foodcourt.plaza.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.foodcourt.plaza.domain.exception.InvalidRestaurantAddress;

class RestaurantAddressTest {

    @Test
    void collapsesInternalSpaces() {
        assertThat(new RestaurantAddress("  Cra 7   # 20-30  ").value()).isEqualTo("Cra 7 # 20-30");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Calle 10", "Cra 7 # 20-30", "Calle 10 / Carrera 5"})
    void acceptsAnAddressWithALetter(String address) {
        assertThat(new RestaurantAddress(address).value()).isEqualTo(address);
    }

    @ParameterizedTest
    @ValueSource(strings = {"***", "A", "123", "Calle 10!"})
    void rejectsAnAddressOutsideTheShape(String address) {
        assertThatThrownBy(() -> new RestaurantAddress(address)).isInstanceOf(InvalidRestaurantAddress.class);
    }
}

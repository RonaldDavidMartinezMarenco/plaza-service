package com.foodcourt.plaza.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.foodcourt.plaza.domain.exception.InvalidOrderQuantity;

class OrderQuantityTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 50})
    void acceptsAQuantityInRange(int quantity) {
        assertThat(new OrderQuantity(quantity).value()).isEqualTo(quantity);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1, 51})
    void rejectsAQuantityOutsideTheRange(Integer quantity) {
        assertThatThrownBy(() -> new OrderQuantity(quantity)).isInstanceOf(InvalidOrderQuantity.class);
    }

    @Test
    void reportsTheAcceptedRange() {
        assertThatThrownBy(() -> new OrderQuantity(0))
                .hasMessage("The quantity must be a whole number from 1 to 50");
    }
}

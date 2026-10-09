package com.foodcourt.plaza.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.foodcourt.plaza.domain.exception.InvalidRestaurantName;

class RestaurantNameTest {

    @Test
    void collapsesSpacesAroundANameThatContainsDigits() {
        assertThat(new RestaurantName("  Sushi   24  ").value()).isEqualTo("Sushi 24");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Plaza-1", "Café", "La 14", "Café & Bar"})
    void acceptsANameWithALetter(String name) {
        assertThat(new RestaurantName(name).value()).isEqualTo(name);
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "  123  ", "***", "A", "Sushi!", "   "})
    void rejectsANameOutsideTheShape(String name) {
        assertThatThrownBy(() -> new RestaurantName(name)).isInstanceOf(InvalidRestaurantName.class);
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new RestaurantName(null)).isInstanceOf(InvalidRestaurantName.class);
    }
}

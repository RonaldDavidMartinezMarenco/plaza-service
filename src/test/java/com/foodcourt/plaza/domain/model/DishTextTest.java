package com.foodcourt.plaza.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.foodcourt.plaza.domain.exception.InvalidDishText;

class DishTextTest {

    @Test
    void trimsSurroundingSpaces() {
        assertThat(new DishText("  Pasta  ").value()).isEqualTo("Pasta");
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new DishText(null)).isInstanceOf(InvalidDishText.class);
    }

    @Test
    void rejectsBlank() {
        assertThatThrownBy(() -> new DishText("")).isInstanceOf(InvalidDishText.class);
    }

    @Test
    void rejectsSpacesOnly() {
        assertThatThrownBy(() -> new DishText("   ")).isInstanceOf(InvalidDishText.class);
    }
}

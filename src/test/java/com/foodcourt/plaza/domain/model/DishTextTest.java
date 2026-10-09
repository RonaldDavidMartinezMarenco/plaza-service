package com.foodcourt.plaza.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.foodcourt.plaza.domain.exception.InvalidDishText;

class DishTextTest {

    @Test
    void collapsesInternalSpaces() {
        assertThat(new DishText("  Pasta   Alfredo  ").value()).isEqualTo("Pasta Alfredo");
    }

    @Test
    void categoryUsesTheShorterMaximum() {
        assertThat(DishText.category("  Main   course  ").value()).isEqualTo("Main course");
    }

    @ParameterizedTest
    @ValueSource(strings = {"A", "***", "123", "Pasta!"})
    void rejectsANameOutsideTheShape(String name) {
        assertThatThrownBy(() -> new DishText(name)).isInstanceOf(InvalidDishText.class);
    }

    @Test
    void rejectsACategoryLongerThanFifty() {
        assertThatThrownBy(() -> DishText.category("a".repeat(51))).isInstanceOf(InvalidDishText.class);
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new DishText(null)).isInstanceOf(InvalidDishText.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejectsBlank(String name) {
        assertThatThrownBy(() -> new DishText(name)).isInstanceOf(InvalidDishText.class);
    }
}

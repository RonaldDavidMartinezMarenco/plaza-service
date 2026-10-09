package com.foodcourt.plaza.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.foodcourt.plaza.domain.exception.InvalidDishDescription;
import com.foodcourt.plaza.domain.exception.InvalidDishText;

class DishDescriptionTest {

    @Test
    void collapsesInternalSpaces() {
        assertThat(new DishDescription("  Ñoquis   al pesto!  ").value()).isEqualTo("Ñoquis al pesto!");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Sopa", "12 sopa", "Hi!"})
    void keepsADescriptionThatIncludesAWord(String description) {
        assertThat(new DishDescription(description).value()).isEqualTo(description);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejectsBlank(String description) {
        assertThatThrownBy(() -> new DishDescription(description)).isInstanceOf(InvalidDishText.class);
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new DishDescription(null)).isInstanceOf(InvalidDishText.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "!!!", "123---", "   999   ", "a", "Hola\nMundo", "<b>Hi</b>"})
    void rejectsADescriptionOutsideTheShape(String description) {
        assertThatThrownBy(() -> new DishDescription(description)).isInstanceOf(InvalidDishDescription.class);
    }
}

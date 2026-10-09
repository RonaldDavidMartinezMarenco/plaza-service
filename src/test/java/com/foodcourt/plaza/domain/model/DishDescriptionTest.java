package com.foodcourt.plaza.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.foodcourt.plaza.domain.exception.InvalidDishDescription;
import com.foodcourt.plaza.domain.exception.InvalidDishText;

class DishDescriptionTest {

    @ParameterizedTest
    @ValueSource(strings = {"Sopa", "  Ñoquis al pesto  ", "12 sopa", "a"})
    void keepsADescriptionThatIncludesAWord(String description) {
        assertThat(new DishDescription(description).value()).isEqualTo(description.trim());
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
    @ValueSource(strings = {"123", "!!!", "123---", "   999   "})
    void rejectsDigitsOrSymbolsAlone(String description) {
        assertThatThrownBy(() -> new DishDescription(description)).isInstanceOf(InvalidDishDescription.class);
    }
}

package com.foodcourt.plaza.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.foodcourt.plaza.domain.exception.InvalidDishText;

class DishListQueryTest {

    private static final PageQuery PAGE = new PageQuery(0, 10);

    @Test
    void keepsAMissingCategory() {
        DishListQuery query = new DishListQuery(PAGE, null);

        assertThat(query.page()).isEqualTo(PAGE);
        assertThat(query.category()).isNull();
    }

    @Test
    void collapsesSurroundingAndRepeatedSpaces() {
        assertThat(new DishListQuery(PAGE, "  Carnes  ").category()).isEqualTo("Carnes");
        assertThat(new DishListQuery(PAGE, "Carnes  BBQ").category()).isEqualTo("Carnes BBQ");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "123", "Carnes!"})
    void rejectsACategoryOutsideTheStoredShape(String category) {
        assertThatThrownBy(() -> new DishListQuery(PAGE, category))
                .isInstanceOf(InvalidDishText.class)
                .hasMessage("The dish text must contain a letter and only the allowed characters");
    }
}

package com.foodcourt.plaza.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.foodcourt.plaza.domain.exception.InvalidDishPrice;

class DishPriceTest {

    @Test
    void acceptsAPositiveInteger() {
        assertThat(new DishPrice(12000).value()).isEqualTo(12000);
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new DishPrice(null)).isInstanceOf(InvalidDishPrice.class);
    }

    @Test
    void rejectsZero() {
        assertThatThrownBy(() -> new DishPrice(0)).isInstanceOf(InvalidDishPrice.class);
    }

    @Test
    void rejectsANegativePrice() {
        assertThatThrownBy(() -> new DishPrice(-1)).isInstanceOf(InvalidDishPrice.class);
    }
}

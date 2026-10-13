package com.foodcourt.plaza.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DishTest {

    @Test
    void withActiveFalseChangesOnlyTheFlag() {
        Dish dish = stored(true);

        Dish disabled = dish.withActive(false);

        assertThat(disabled.active()).isFalse();
        assertThat(disabled.id()).isEqualTo(dish.id());
        assertThat(disabled.restaurantId()).isEqualTo(dish.restaurantId());
        assertThat(disabled.name()).isEqualTo(dish.name());
        assertThat(disabled.price()).isEqualTo(dish.price());
        assertThat(disabled.description()).isEqualTo(dish.description());
        assertThat(disabled.imageUrl()).isEqualTo(dish.imageUrl());
        assertThat(disabled.category()).isEqualTo(dish.category());
    }

    @Test
    void withActiveTrueChangesOnlyTheFlag() {
        Dish dish = stored(false);

        Dish enabled = dish.withActive(true);

        assertThat(enabled.active()).isTrue();
        assertThat(enabled.id()).isEqualTo(dish.id());
        assertThat(enabled.restaurantId()).isEqualTo(dish.restaurantId());
        assertThat(enabled.name()).isEqualTo(dish.name());
        assertThat(enabled.price()).isEqualTo(dish.price());
        assertThat(enabled.description()).isEqualTo(dish.description());
        assertThat(enabled.imageUrl()).isEqualTo(dish.imageUrl());
        assertThat(enabled.category()).isEqualTo(dish.category());
    }

    private static Dish stored(boolean active) {
        return new Dish(
                21L,
                4L,
                "Pasta",
                12000,
                "Cream sauce",
                "https://cdn.example.com/dishes/pasta.png",
                "Main",
                active);
    }
}

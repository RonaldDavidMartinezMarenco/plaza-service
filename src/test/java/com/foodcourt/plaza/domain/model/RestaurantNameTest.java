package com.foodcourt.plaza.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.foodcourt.plaza.domain.exception.InvalidRestaurantName;

class RestaurantNameTest {

    @Test
    void trimsANameThatContainsDigits() {
        assertThat(new RestaurantName("  Sushi 24  ").value()).isEqualTo("Sushi 24");
    }

    @Test
    void acceptsANameThatIsNotOnlyDigits() {
        assertThat(new RestaurantName("Plaza-1").value()).isEqualTo("Plaza-1");
    }

    @Test
    void rejectsDigitsOnly() {
        assertThatThrownBy(() -> new RestaurantName("123")).isInstanceOf(InvalidRestaurantName.class);
    }

    @Test
    void rejectsDigitsSurroundedBySpaces() {
        assertThatThrownBy(() -> new RestaurantName("  123  ")).isInstanceOf(InvalidRestaurantName.class);
    }

    @Test
    void rejectsBlank() {
        assertThatThrownBy(() -> new RestaurantName("   ")).isInstanceOf(InvalidRestaurantName.class);
    }
}

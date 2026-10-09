package com.foodcourt.plaza.domain.model;

import com.foodcourt.plaza.domain.exception.InvalidDishPrice;

/** Dish price in whole currency units. Zero, a negative number, and null are rejected. */
public record DishPrice(Integer value) {

    public DishPrice {
        if (value == null || value <= 0) {
            throw new InvalidDishPrice();
        }
    }
}

package com.foodcourt.plaza.application.usecase;

import java.util.List;

import com.foodcourt.plaza.domain.exception.DishInactive;
import com.foodcourt.plaza.domain.exception.DishNotFound;
import com.foodcourt.plaza.domain.exception.DishRestaurantMismatch;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.spi.DishRepository;

/**
 * Every line is an active dish of the order's restaurant.
 * The first failing line, in request order, stops the check.
 */
final class OrderMenuRule {

    private final DishRepository dishes;

    OrderMenuRule(DishRepository dishes) {
        this.dishes = dishes;
    }

    void requireOrderable(long restaurantId, List<OrderLine> lines) {
        for (OrderLine line : lines) {
            requireDish(restaurantId, line.dishId());
        }
    }

    private void requireDish(long restaurantId, long dishId) {
        Dish dish = dishes.findById(dishId).orElseThrow(DishNotFound::new);
        requireSameRestaurant(restaurantId, dish);
        requireActive(dish);
    }

    private static void requireSameRestaurant(long restaurantId, Dish dish) {
        if (dish.restaurantId() == null || dish.restaurantId() != restaurantId) {
            throw new DishRestaurantMismatch();
        }
    }

    private static void requireActive(Dish dish) {
        if (!dish.active()) {
            throw new DishInactive();
        }
    }
}

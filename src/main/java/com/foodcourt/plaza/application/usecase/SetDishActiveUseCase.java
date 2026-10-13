package com.foodcourt.plaza.application.usecase;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.SetDishActive;
import com.foodcourt.plaza.domain.exception.DishNotFound;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.DishRepository;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

/**
 * Sets whether a dish is offered. Checks run in this order, and a rejection
 * does not write: the caller is an {@link Role#OWNER}; the dish exists; that
 * caller owns its restaurant and the owner account is still an owner; then one
 * column is written. The same state is written again, so a retry stays on the
 * requested value. Name, price, description, image, category, and restaurant stay
 * as stored.
 */
@Service
public final class SetDishActiveUseCase implements SetDishActive {

    private final DishRepository dishes;
    private final RestaurantOwnershipRule ownership;

    public SetDishActiveUseCase(
            DishRepository dishes,
            RestaurantRepository restaurants,
            OwnerDirectory owners) {
        this.dishes = dishes;
        this.ownership = new RestaurantOwnershipRule(restaurants, new OwnerAccountRule(owners));
    }

    @Override
    public Dish setActive(Caller caller, long dishId, boolean active) {
        ownership.requireOwnerRole(caller);
        Dish current = requireDish(dishId);
        ownership.requireOwnedBy(caller, current.restaurantId());
        write(dishId, active);
        return current.withActive(active);
    }

    private Dish requireDish(long dishId) {
        return dishes.findById(dishId).orElseThrow(DishNotFound::new);
    }

    private void write(long dishId, boolean active) {
        if (!dishes.updateActive(dishId, active)) {
            throw new DishNotFound();
        }
    }
}

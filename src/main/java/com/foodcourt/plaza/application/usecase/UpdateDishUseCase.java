package com.foodcourt.plaza.application.usecase;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.UpdateDish;
import com.foodcourt.plaza.domain.exception.DishNotFound;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.DishDescription;
import com.foodcourt.plaza.domain.model.DishPrice;
import com.foodcourt.plaza.domain.model.DishUpdate;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.DishRepository;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

/**
 * Updates a dish's price and description. Checks run in this order, and a
 * rejection does not save: a price greater than zero; a description in the
 * description shape; the caller is an {@link Role#OWNER}; an existing dish;
 * then that caller owns the restaurant and that restaurant's owner account is
 * still an owner. When the price and description already match the stored dish,
 * that dish is returned and nothing is saved. Name, image, category, restaurant,
 * and active stay as stored.
 */
@Service
public final class UpdateDishUseCase implements UpdateDish {

    private final DishRepository dishes;
    private final RestaurantOwnershipRule ownership;

    public UpdateDishUseCase(
            DishRepository dishes,
            RestaurantRepository restaurants,
            OwnerDirectory owners) {
        this.dishes = dishes;
        this.ownership = new RestaurantOwnershipRule(restaurants, new OwnerAccountRule(owners));
    }

    @Override
    public Dish update(Caller caller, long dishId, DishUpdate change) {
        DishPrice price = requirePrice(change);
        DishDescription description = requireDescription(change);
        ownership.requireOwnerRole(caller);
        Dish current = requireDish(dishId);
        ownership.requireOwnedBy(caller, current.restaurantId());
        return stored(current, price, description);
    }

    private static DishPrice requirePrice(DishUpdate change) {
        return new DishPrice(change.price());
    }

    private static DishDescription requireDescription(DishUpdate change) {
        return new DishDescription(change.description());
    }

    private Dish requireDish(long dishId) {
        return dishes.findById(dishId).orElseThrow(DishNotFound::new);
    }

    private Dish stored(Dish current, DishPrice price, DishDescription description) {
        if (samePrice(current, price) && sameDescription(current, description)) {
            return current;
        }
        return dishes.save(revised(current, price, description));
    }

    private static boolean samePrice(Dish current, DishPrice price) {
        return current.price().equals(price.value());
    }

    private static boolean sameDescription(Dish current, DishDescription description) {
        return current.description().equals(description.value());
    }

    private static Dish revised(Dish current, DishPrice price, DishDescription description) {
        return new Dish(
                current.id(),
                current.restaurantId(),
                current.name(),
                price.value(),
                description.value(),
                current.imageUrl(),
                current.category(),
                current.active());
    }
}

package com.foodcourt.plaza.application.usecase;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.UpdateDish;
import com.foodcourt.plaza.domain.exception.AccessDenied;
import com.foodcourt.plaza.domain.exception.DishNotFound;
import com.foodcourt.plaza.domain.exception.OwnerNotFound;
import com.foodcourt.plaza.domain.exception.OwnerRoleRequired;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.DishDescription;
import com.foodcourt.plaza.domain.model.DishPrice;
import com.foodcourt.plaza.domain.model.DishUpdate;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.DishRepository;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

/**
 * Updates a dish's price and description. Checks run in this order, and a
 * rejection does not save: a price greater than zero; a description in the
 * description shape; an existing dish; that dish's restaurant; the caller is an
 * {@link Role#OWNER} of that restaurant; then that owner account. When the price and
 * description already match the stored dish, that dish is returned and nothing
 * is saved. Name, image, category, restaurant, and active stay as stored.
 */
@Service
public final class UpdateDishUseCase implements UpdateDish {

    private final DishRepository dishes;
    private final RestaurantRepository restaurants;
    private final OwnerDirectory owners;

    public UpdateDishUseCase(
            DishRepository dishes,
            RestaurantRepository restaurants,
            OwnerDirectory owners) {
        this.dishes = dishes;
        this.restaurants = restaurants;
        this.owners = owners;
    }

    @Override
    public Dish update(Caller caller, long dishId, DishUpdate change) {
        DishPrice price = requirePrice(change);
        DishDescription description = requireDescription(change);
        Dish current = requireDish(dishId);
        Restaurant restaurant = requireRestaurant(current.restaurantId());
        requireOwnerCaller(caller);
        requireOwnsRestaurant(caller, restaurant);
        requireOwner(restaurant.ownerId());
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

    private Restaurant requireRestaurant(Long restaurantId) {
        return restaurants.findById(restaurantId).orElseThrow(RestaurantNotFound::new);
    }

    private static void requireOwnerCaller(Caller caller) {
        if (caller.role() != Role.OWNER) {
            throw new AccessDenied();
        }
    }

    private static void requireOwnsRestaurant(Caller caller, Restaurant restaurant) {
        if (!owns(caller, restaurant)) {
            throw new AccessDenied();
        }
    }

    private static boolean owns(Caller caller, Restaurant restaurant) {
        return caller.id() == restaurant.ownerId();
    }

    private void requireOwner(Long ownerId) {
        Account account = owners.findById(ownerId).orElseThrow(OwnerNotFound::new);
        if (account.role() != Role.OWNER) {
            throw new OwnerRoleRequired();
        }
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

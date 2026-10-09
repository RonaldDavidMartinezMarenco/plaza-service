package com.foodcourt.plaza.application.usecase;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.CreateDish;
import com.foodcourt.plaza.domain.exception.OwnerNotFound;
import com.foodcourt.plaza.domain.exception.OwnerRoleRequired;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.DishDescription;
import com.foodcourt.plaza.domain.model.DishPrice;
import com.foodcourt.plaza.domain.model.DishText;
import com.foodcourt.plaza.domain.model.NewDish;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.DishRepository;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

/**
 * Creates a dish. Checks run in this order, and a rejection does not save:
 * a trimmed name and category; a trimmed description that includes a word;
 * a price greater than zero; an existing restaurant; then that restaurant's
 * owner account with role {@link Role#OWNER}. A new dish is always active.
 */
@Service
public final class CreateDishUseCase implements CreateDish {

    private final DishRepository dishes;
    private final RestaurantRepository restaurants;
    private final OwnerDirectory owners;

    public CreateDishUseCase(
            DishRepository dishes,
            RestaurantRepository restaurants,
            OwnerDirectory owners) {
        this.dishes = dishes;
        this.restaurants = restaurants;
        this.owners = owners;
    }

    @Override
    public Dish create(NewDish draft) {
        DishText name = requireName(draft);
        DishDescription description = requireDescription(draft);
        DishText category = requireCategory(draft);
        DishPrice price = requirePrice(draft);
        Restaurant restaurant = requireRestaurant(draft.restaurantId());
        requireOwner(restaurant.ownerId());
        return dishes.save(stored(draft, name, price, description, category, restaurant));
    }

    private static DishText requireName(NewDish draft) {
        return new DishText(draft.name());
    }

    private static DishDescription requireDescription(NewDish draft) {
        return new DishDescription(draft.description());
    }

    private static DishText requireCategory(NewDish draft) {
        return new DishText(draft.category());
    }

    private static DishPrice requirePrice(NewDish draft) {
        return new DishPrice(draft.price());
    }

    private Restaurant requireRestaurant(Long restaurantId) {
        return restaurants.findById(restaurantId).orElseThrow(RestaurantNotFound::new);
    }

    private void requireOwner(Long ownerId) {
        Account account = owners.findById(ownerId).orElseThrow(OwnerNotFound::new);
        if (account.role() != Role.OWNER) {
            throw new OwnerRoleRequired();
        }
    }

    private static Dish stored(
            NewDish draft,
            DishText name,
            DishPrice price,
            DishDescription description,
            DishText category,
            Restaurant restaurant) {
        return new Dish(
                null,
                restaurant.id(),
                name.value(),
                price.value(),
                description.value(),
                draft.imageUrl(),
                category.value(),
                true);
    }
}

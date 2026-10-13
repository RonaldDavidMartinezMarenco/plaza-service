package com.foodcourt.plaza.application.usecase;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.CreateDish;
import com.foodcourt.plaza.domain.model.Caller;
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
 * a name and category in the name shape; a description in the description shape;
 * a price greater than zero; the caller is an {@link Role#OWNER}; then that caller
 * owns the restaurant and that restaurant's owner account is still an owner.
 * A new dish is always active.
 */
@Service
public final class CreateDishUseCase implements CreateDish {

    private final DishRepository dishes;
    private final RestaurantOwnershipRule ownership;

    public CreateDishUseCase(
            DishRepository dishes,
            RestaurantRepository restaurants,
            OwnerDirectory owners) {
        this.dishes = dishes;
        this.ownership = new RestaurantOwnershipRule(restaurants, new OwnerAccountRule(owners));
    }

    @Override
    public Dish create(Caller caller, NewDish draft) {
        DishText name = requireName(draft);
        DishDescription description = requireDescription(draft);
        DishText category = requireCategory(draft);
        DishPrice price = requirePrice(draft);
        ownership.requireOwnerRole(caller);
        Restaurant restaurant = ownership.requireOwnedBy(caller, draft.restaurantId());
        return dishes.save(stored(draft, name, price, description, category, restaurant));
    }

    private static DishText requireName(NewDish draft) {
        return new DishText(draft.name());
    }

    private static DishDescription requireDescription(NewDish draft) {
        return new DishDescription(draft.description());
    }

    private static DishText requireCategory(NewDish draft) {
        return DishText.category(draft.category());
    }

    private static DishPrice requirePrice(NewDish draft) {
        return new DishPrice(draft.price());
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

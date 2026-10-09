package com.foodcourt.plaza.infrastructure.input.rest.mapper;

import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.NewDish;
import com.foodcourt.plaza.infrastructure.input.rest.dto.CreateDishRequest;
import com.foodcourt.plaza.infrastructure.input.rest.dto.DishResponse;

/** Copies fields between the HTTP contract and the domain. Applies no rule. */
public final class DishRestMapper {

    public NewDish toNewDish(CreateDishRequest request) {
        return new NewDish(
                request.name(),
                request.price(),
                request.description(),
                request.imageUrl(),
                request.category(),
                request.restaurantId());
    }

    public DishResponse toResponse(Dish dish) {
        return new DishResponse(
                dish.id(),
                dish.name(),
                dish.price(),
                dish.description(),
                dish.imageUrl(),
                dish.category(),
                dish.restaurantId(),
                dish.active());
    }
}

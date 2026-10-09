package com.foodcourt.plaza.application.handler;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.CreateDish;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.NewDish;

/**
 * Entry the controller calls. Validation stays on the request DTO and the
 * rules stay in {@link CreateDish}; this type only forwards the call.
 */
@Service
public final class CreateDishHandler {

    private final CreateDish createDish;

    public CreateDishHandler(CreateDish createDish) {
        this.createDish = createDish;
    }

    public Dish create(NewDish dish) {
        return createDish.create(dish);
    }
}

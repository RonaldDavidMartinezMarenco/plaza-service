package com.foodcourt.plaza.application.handler;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.UpdateDish;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.DishUpdate;

/**
 * Entry the controller calls. Validation stays on the request DTO and the
 * rules stay in {@link UpdateDish}; this type only forwards the call.
 */
@Service
public final class UpdateDishHandler {

    private final UpdateDish updateDish;

    public UpdateDishHandler(UpdateDish updateDish) {
        this.updateDish = updateDish;
    }

    public Dish update(long dishId, DishUpdate change) {
        return updateDish.update(dishId, change);
    }
}

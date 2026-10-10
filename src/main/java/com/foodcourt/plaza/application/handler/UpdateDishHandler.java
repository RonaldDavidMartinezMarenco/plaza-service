package com.foodcourt.plaza.application.handler;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.UpdateDish;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.DishUpdate;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

/**
 * Entry the controller calls. Validation stays on the request DTO and the
 * rules stay in {@link UpdateDish}. This type reads the caller and forwards the call.
 */
@Service
public final class UpdateDishHandler {

    private final UpdateDish updateDish;
    private final CurrentCaller currentCaller;

    public UpdateDishHandler(UpdateDish updateDish, CurrentCaller currentCaller) {
        this.updateDish = updateDish;
        this.currentCaller = currentCaller;
    }

    public Dish update(long dishId, DishUpdate change) {
        return updateDish.update(currentCaller.require(), dishId, change);
    }
}

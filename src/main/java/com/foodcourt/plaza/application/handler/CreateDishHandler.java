package com.foodcourt.plaza.application.handler;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.CreateDish;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.NewDish;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

/**
 * Entry the controller calls. Validation stays on the request DTO and the
 * rules stay in {@link CreateDish}. This type reads the caller and forwards the call.
 */
@Service
public final class CreateDishHandler {

    private final CreateDish createDish;
    private final CurrentCaller currentCaller;

    public CreateDishHandler(CreateDish createDish, CurrentCaller currentCaller) {
        this.createDish = createDish;
        this.currentCaller = currentCaller;
    }

    public Dish create(NewDish dish) {
        return createDish.create(currentCaller.require(), dish);
    }
}

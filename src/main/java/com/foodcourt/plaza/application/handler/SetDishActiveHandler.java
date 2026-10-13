package com.foodcourt.plaza.application.handler;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.SetDishActive;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

/**
 * Entry the controller calls. Validation stays on the request DTO and the
 * rules stay in {@link SetDishActive}. This type reads the caller and forwards the call.
 */
@Service
public final class SetDishActiveHandler {

    private final SetDishActive setDishActive;
    private final CurrentCaller currentCaller;

    public SetDishActiveHandler(SetDishActive setDishActive, CurrentCaller currentCaller) {
        this.setDishActive = setDishActive;
        this.currentCaller = currentCaller;
    }

    public Dish setActive(long dishId, boolean active) {
        return setDishActive.setActive(currentCaller.require(), dishId, active);
    }
}

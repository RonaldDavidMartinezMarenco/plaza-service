package com.foodcourt.plaza.application.handler;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.CreateRestaurant;
import com.foodcourt.plaza.domain.model.NewRestaurant;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

/**
 * Entry the controller calls. Validation stays on the request DTO and the
 * rules stay in {@link CreateRestaurant}. This type reads the caller and forwards the call.
 */
@Service
public final class CreateRestaurantHandler {

    private final CreateRestaurant createRestaurant;
    private final CurrentCaller currentCaller;

    public CreateRestaurantHandler(CreateRestaurant createRestaurant, CurrentCaller currentCaller) {
        this.createRestaurant = createRestaurant;
        this.currentCaller = currentCaller;
    }

    public Restaurant create(NewRestaurant restaurant) {
        return createRestaurant.create(currentCaller.require(), restaurant);
    }
}

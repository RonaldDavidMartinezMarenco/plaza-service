package com.foodcourt.plaza.application.handler;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.CreateRestaurant;
import com.foodcourt.plaza.domain.model.NewRestaurant;
import com.foodcourt.plaza.domain.model.Restaurant;

/**
 * Entry the controller calls. Validation stays on the request DTO and the
 * rules stay in {@link CreateRestaurant}; this type only forwards the call.
 */
@Service
public final class CreateRestaurantHandler {

    private final CreateRestaurant createRestaurant;

    public CreateRestaurantHandler(CreateRestaurant createRestaurant) {
        this.createRestaurant = createRestaurant;
    }

    public Restaurant create(NewRestaurant restaurant) {
        return createRestaurant.create(restaurant);
    }
}

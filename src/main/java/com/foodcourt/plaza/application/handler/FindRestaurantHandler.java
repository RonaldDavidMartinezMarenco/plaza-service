package com.foodcourt.plaza.application.handler;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.FindRestaurant;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

/**
 * Entry the controller calls. The rules stay in {@link FindRestaurant}.
 * This type reads the caller and forwards the call.
 */
@Service
public final class FindRestaurantHandler {

    private final FindRestaurant findRestaurant;
    private final CurrentCaller currentCaller;

    public FindRestaurantHandler(FindRestaurant findRestaurant, CurrentCaller currentCaller) {
        this.findRestaurant = findRestaurant;
        this.currentCaller = currentCaller;
    }

    public Restaurant find(long id) {
        return findRestaurant.find(currentCaller.require(), id);
    }
}

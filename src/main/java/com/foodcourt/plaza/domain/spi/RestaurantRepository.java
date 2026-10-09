package com.foodcourt.plaza.domain.spi;

import com.foodcourt.plaza.domain.model.Restaurant;

/** Persistence port for {@code restaurant}. */
public interface RestaurantRepository {

    boolean existsByNit(String nit);

    Restaurant save(Restaurant restaurant);
}

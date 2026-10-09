package com.foodcourt.plaza.domain.spi;

import java.util.Optional;

import com.foodcourt.plaza.domain.model.Restaurant;

/** Persistence port for {@code restaurant}. */
public interface RestaurantRepository {

    boolean existsByNit(String nit);

    /** {@code true} when {@code lower(name)} is already stored. {@code Café} and {@code Cafe} differ. */
    boolean existsByName(String name);

    Optional<Restaurant> findById(long id);

    Restaurant save(Restaurant restaurant);
}

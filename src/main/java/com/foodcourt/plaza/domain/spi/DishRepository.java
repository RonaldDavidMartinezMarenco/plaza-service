package com.foodcourt.plaza.domain.spi;

import java.util.Optional;

import com.foodcourt.plaza.domain.model.Dish;

/** Persistence port for {@code dish}. */
public interface DishRepository {

    Optional<Dish> findById(long id);

    Dish save(Dish dish);

    /**
     * Writes only {@code active}. Returns whether a row with this id existed.
     * A full-row save would put back a price or flag changed by another request
     * after this one read the dish.
     */
    boolean updateActive(long id, boolean active);
}

package com.foodcourt.plaza.domain.spi;

import java.util.Optional;

import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.DishListQuery;
import com.foodcourt.plaza.domain.model.DishSummary;
import com.foodcourt.plaza.domain.model.PageResult;

/** Persistence port for {@code dish}. */
public interface DishRepository {

    Optional<Dish> findById(long id);

    /**
     * One page of active dishes, ordered by name and then id.
     * A null category lists every active dish of the restaurant.
     */
    PageResult<DishSummary> findActivePage(long restaurantId, DishListQuery query);

    Dish save(Dish dish);

    /**
     * Writes only {@code active}. Returns whether a row with this id existed.
     * A full-row save would put back a price or flag changed by another request
     * after this one read the dish.
     */
    boolean updateActive(long id, boolean active);
}

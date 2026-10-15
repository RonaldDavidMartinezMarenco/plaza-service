package com.foodcourt.plaza.domain.spi;

import java.util.Optional;

import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.RestaurantSummary;

/** Persistence port for {@code restaurant}. */
public interface RestaurantRepository {

    boolean existsByNit(String nit);

    /** {@code true} when {@code lower(name)} is already stored. {@code Café} and {@code Cafe} differ. */
    boolean existsByName(String name);

    Optional<Restaurant> findById(long id);

    /** One page ordered by name, then id. The order is part of this contract. */
    PageResult<RestaurantSummary> findPageOrderedByName(PageQuery query);

    Restaurant save(Restaurant restaurant);
}

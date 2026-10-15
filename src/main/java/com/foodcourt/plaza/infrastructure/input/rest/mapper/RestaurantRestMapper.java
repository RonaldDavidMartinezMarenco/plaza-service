package com.foodcourt.plaza.infrastructure.input.rest.mapper;

import java.util.List;

import com.foodcourt.plaza.domain.model.NewRestaurant;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.RestaurantSummary;
import com.foodcourt.plaza.infrastructure.input.rest.dto.CreateRestaurantRequest;
import com.foodcourt.plaza.infrastructure.input.rest.dto.PageResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.RestaurantOwnershipResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.RestaurantResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.RestaurantSummaryResponse;

/** Copies fields between the HTTP contract and the domain. Applies no rule. */
public final class RestaurantRestMapper {

    public NewRestaurant toNewRestaurant(CreateRestaurantRequest request) {
        return new NewRestaurant(
                request.name(),
                request.nit(),
                request.address(),
                request.phone(),
                request.logoUrl(),
                request.ownerId());
    }

    public RestaurantResponse toResponse(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.id(),
                restaurant.name(),
                restaurant.nit(),
                restaurant.address(),
                restaurant.phone(),
                restaurant.logoUrl(),
                restaurant.ownerId());
    }

    public RestaurantOwnershipResponse toOwnershipResponse(Restaurant restaurant) {
        return new RestaurantOwnershipResponse(restaurant.id(), restaurant.ownerId());
    }

    public PageQuery toPageQuery(int page, int size) {
        return new PageQuery(page, size);
    }

    public PageResponse<RestaurantSummaryResponse> toSummaryPage(PageResult<RestaurantSummary> page) {
        List<RestaurantSummaryResponse> items = page.items().stream().map(this::toSummary).toList();
        return new PageResponse<>(items, page.page(), page.size(), page.totalItems(), page.totalPages());
    }

    private RestaurantSummaryResponse toSummary(RestaurantSummary restaurant) {
        return new RestaurantSummaryResponse(restaurant.id(), restaurant.name(), restaurant.logoUrl());
    }
}

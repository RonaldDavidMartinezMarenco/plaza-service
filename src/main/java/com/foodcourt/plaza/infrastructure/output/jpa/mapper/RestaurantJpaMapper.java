package com.foodcourt.plaza.infrastructure.output.jpa.mapper;

import java.util.List;

import org.springframework.data.domain.Page;

import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.RestaurantSummary;
import com.foodcourt.plaza.infrastructure.output.jpa.entity.RestaurantEntity;
import com.foodcourt.plaza.infrastructure.output.jpa.entity.RestaurantSummaryView;

/** Copies fields between {@link RestaurantEntity} and {@link Restaurant}. Applies no rule. */
public final class RestaurantJpaMapper {

    public RestaurantEntity toEntity(Restaurant restaurant) {
        return new RestaurantEntity(
                restaurant.id(),
                restaurant.name(),
                restaurant.nit(),
                restaurant.address(),
                restaurant.phone(),
                restaurant.logoUrl(),
                restaurant.ownerId());
    }

    public Restaurant toDomain(RestaurantEntity entity) {
        return new Restaurant(
                entity.getId(),
                entity.getName(),
                entity.getNit(),
                entity.getAddress(),
                entity.getPhone(),
                entity.getLogoUrl(),
                entity.getOwnerId());
    }

    public RestaurantSummary toSummary(RestaurantSummaryView view) {
        return new RestaurantSummary(view.getId(), view.getName(), view.getLogoUrl());
    }

    public PageResult<RestaurantSummary> toSummaryPage(Page<RestaurantSummaryView> page) {
        List<RestaurantSummary> items = page.getContent().stream().map(this::toSummary).toList();
        return new PageResult<>(items, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}

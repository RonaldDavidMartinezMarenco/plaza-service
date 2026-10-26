package com.foodcourt.plaza.infrastructure.output.jpa.mapper;

import java.util.List;

import org.springframework.data.domain.Page;

import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.DishSummary;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.infrastructure.output.jpa.entity.DishEntity;

/** Copies fields between {@link DishEntity} and {@link Dish}. Applies no rule. */
public final class DishJpaMapper {

    public DishEntity toEntity(Dish dish) {
        return new DishEntity(
                dish.id(),
                dish.restaurantId(),
                dish.name(),
                dish.price(),
                dish.description(),
                dish.imageUrl(),
                dish.category(),
                dish.active());
    }

    public Dish toDomain(DishEntity entity) {
        return new Dish(
                entity.getId(),
                entity.getRestaurantId(),
                entity.getName(),
                entity.getPrice(),
                entity.getDescription(),
                entity.getImageUrl(),
                entity.getCategory(),
                entity.isActive());
    }

    public DishSummary toSummary(DishEntity entity) {
        return new DishSummary(
                entity.getId(),
                entity.getRestaurantId(),
                entity.getName(),
                entity.getPrice(),
                entity.getDescription(),
                entity.getImageUrl(),
                entity.getCategory());
    }

    public PageResult<DishSummary> toSummaryPage(Page<DishEntity> page) {
        List<DishSummary> items = page.getContent().stream().map(this::toSummary).toList();
        return new PageResult<>(items, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}

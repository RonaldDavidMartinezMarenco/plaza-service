package com.foodcourt.plaza.infrastructure.input.rest.mapper;

import java.util.List;

import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.DishSummary;
import com.foodcourt.plaza.domain.model.DishUpdate;
import com.foodcourt.plaza.domain.model.NewDish;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.infrastructure.input.rest.dto.CreateDishRequest;
import com.foodcourt.plaza.infrastructure.input.rest.dto.DishResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.DishSummaryResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.PageResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.UpdateDishRequest;

/** Copies fields between the HTTP contract and the domain. Applies no rule. */
public final class DishRestMapper {

    public NewDish toNewDish(CreateDishRequest request) {
        return new NewDish(
                request.name(),
                request.price(),
                request.description(),
                request.imageUrl(),
                request.category(),
                request.restaurantId());
    }

    public DishUpdate toDishUpdate(UpdateDishRequest request) {
        return new DishUpdate(request.price(), request.description());
    }

    public DishResponse toResponse(Dish dish) {
        return new DishResponse(
                dish.id(),
                dish.name(),
                dish.price(),
                dish.description(),
                dish.imageUrl(),
                dish.category(),
                dish.restaurantId(),
                dish.active());
    }

    public PageQuery toPageQuery(int page, int size) {
        return new PageQuery(page, size);
    }

    public PageResponse<DishSummaryResponse> toSummaryPage(PageResult<DishSummary> page) {
        List<DishSummaryResponse> items = page.items().stream().map(this::toSummary).toList();
        return new PageResponse<>(items, page.page(), page.size(), page.totalItems(), page.totalPages());
    }

    private DishSummaryResponse toSummary(DishSummary dish) {
        return new DishSummaryResponse(
                dish.id(),
                dish.restaurantId(),
                dish.name(),
                dish.price(),
                dish.description(),
                dish.imageUrl(),
                dish.category());
    }
}

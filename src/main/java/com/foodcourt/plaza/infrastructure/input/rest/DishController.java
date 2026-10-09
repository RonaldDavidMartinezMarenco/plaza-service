package com.foodcourt.plaza.infrastructure.input.rest;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.foodcourt.plaza.application.handler.CreateDishHandler;
import com.foodcourt.plaza.application.handler.UpdateDishHandler;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.infrastructure.input.rest.dto.CreateDishRequest;
import com.foodcourt.plaza.infrastructure.input.rest.dto.DishResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.UpdateDishRequest;
import com.foodcourt.plaza.infrastructure.input.rest.mapper.DishRestMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

/**
 * Creates and updates a dish. Authentication is added in a later story;
 * until then the security filter permits these calls.
 * The class is not final: {@code @Validated} on the path id needs a CGLIB subclass.
 */
@RestController
@Validated
@Tag(name = "Dishes")
class DishController {

    private final CreateDishHandler createDish;
    private final UpdateDishHandler updateDish;
    private final DishRestMapper mapper = new DishRestMapper();

    DishController(CreateDishHandler createDish, UpdateDishHandler updateDish) {
        this.createDish = createDish;
        this.updateDish = updateDish;
    }

    @PostMapping("/dishes")
    @Operation(summary = "Create a dish")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dish created"),
            @ApiResponse(responseCode = "400", description = "Invalid body, text, or price"),
            @ApiResponse(responseCode = "403", description = "The restaurant owner is not an owner"),
            @ApiResponse(responseCode = "404", description = "Restaurant or owner account does not exist"),
            @ApiResponse(responseCode = "503", description = "The user service is unavailable")
    })
    ResponseEntity<DishResponse> create(@Valid @RequestBody CreateDishRequest request) {
        Dish dish = createDish.create(mapper.toNewDish(request));
        URI location = URI.create("/dishes/" + dish.id());
        return ResponseEntity.created(location).body(mapper.toResponse(dish));
    }

    @PutMapping("/dishes/{id}")
    @Operation(summary = "Update a dish price and description")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dish returned with that price and description"),
            @ApiResponse(responseCode = "400", description = "Invalid id, body, text, or price"),
            @ApiResponse(responseCode = "403", description = "The restaurant owner is not an owner"),
            @ApiResponse(responseCode = "404", description = "Dish, restaurant, or owner account does not exist"),
            @ApiResponse(responseCode = "503", description = "The user service is unavailable")
    })
    ResponseEntity<DishResponse> update(
            @PathVariable @Positive long id,
            @Valid @RequestBody UpdateDishRequest request) {
        Dish dish = updateDish.update(id, mapper.toDishUpdate(request));
        return ResponseEntity.ok(mapper.toResponse(dish));
    }
}

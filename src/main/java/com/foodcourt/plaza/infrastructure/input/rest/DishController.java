package com.foodcourt.plaza.infrastructure.input.rest;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.foodcourt.plaza.application.handler.CreateDishHandler;
import com.foodcourt.plaza.application.handler.SetDishActiveHandler;
import com.foodcourt.plaza.application.handler.UpdateDishHandler;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.infrastructure.input.rest.dto.CreateDishRequest;
import com.foodcourt.plaza.infrastructure.input.rest.dto.DishResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.SetDishActiveRequest;
import com.foodcourt.plaza.infrastructure.input.rest.dto.UpdateDishRequest;
import com.foodcourt.plaza.infrastructure.input.rest.mapper.DishRestMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

/**
 * Creates, updates, and enables or disables a dish. The caller must be the owner
 * of that restaurant. The class is not final: {@code @Validated} and method
 * security need a CGLIB subclass.
 */
@RestController
@Validated
@Tag(name = "Dishes")
@SecurityRequirement(name = "bearerAuth")
class DishController {

    private final CreateDishHandler createDish;
    private final UpdateDishHandler updateDish;
    private final SetDishActiveHandler setDishActive;
    private final DishRestMapper mapper = new DishRestMapper();

    DishController(CreateDishHandler createDish, UpdateDishHandler updateDish, SetDishActiveHandler setDishActive) {
        this.createDish = createDish;
        this.updateDish = updateDish;
        this.setDishActive = setDishActive;
    }

    @PostMapping("/dishes")
    @Operation(summary = "Create a dish")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dish created"),
            @ApiResponse(responseCode = "400", description = "Invalid body, text, or price"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
            @ApiResponse(responseCode = "403", description = "The caller is not the owner of this restaurant"),
            @ApiResponse(responseCode = "404", description = "Restaurant or owner account does not exist"),
            @ApiResponse(responseCode = "503", description = "The user service is unavailable")
    })
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<DishResponse> create(@Valid @RequestBody CreateDishRequest request) {
        Dish dish = createDish.create(mapper.toNewDish(request));
        URI location = URI.create("/dishes/" + dish.id());
        return ResponseEntity.created(location).body(mapper.toResponse(dish));
    }

    @PutMapping("/dishes/{id}")
    @Operation(summary = "Update a dish price and description")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dish returned with that price and description"),
            @ApiResponse(responseCode = "400", description = "Invalid id, body, text, or price"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
            @ApiResponse(responseCode = "403", description = "The caller is not the owner of this restaurant"),
            @ApiResponse(responseCode = "404", description = "Dish, restaurant, or owner account does not exist"),
            @ApiResponse(responseCode = "503", description = "The user service is unavailable")
    })
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<DishResponse> update(
            @PathVariable @Positive long id,
            @Valid @RequestBody UpdateDishRequest request) {
        Dish dish = updateDish.update(id, mapper.toDishUpdate(request));
        return ResponseEntity.ok(mapper.toResponse(dish));
    }

    @PutMapping("/dishes/{id}/active")
    @Operation(summary = "Enable or disable a dish")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dish returned with the requested active state"),
            @ApiResponse(responseCode = "400", description = "Invalid id or body"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
            @ApiResponse(responseCode = "403", description = "The caller is not the owner of this restaurant"),
            @ApiResponse(responseCode = "404", description = "Dish, restaurant, or owner account does not exist"),
            @ApiResponse(responseCode = "415", description = "The body is not JSON"),
            @ApiResponse(responseCode = "503", description = "The user service is unavailable")
    })
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<DishResponse> setActive(
            @PathVariable @Positive long id,
            @Valid @RequestBody SetDishActiveRequest request) {
        Dish dish = setDishActive.setActive(id, request.active());
        return ResponseEntity.ok(mapper.toResponse(dish));
    }
}

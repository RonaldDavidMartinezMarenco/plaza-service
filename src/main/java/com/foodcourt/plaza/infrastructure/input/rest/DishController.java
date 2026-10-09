package com.foodcourt.plaza.infrastructure.input.rest;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.foodcourt.plaza.application.handler.CreateDishHandler;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.infrastructure.input.rest.dto.CreateDishRequest;
import com.foodcourt.plaza.infrastructure.input.rest.dto.DishResponse;
import com.foodcourt.plaza.infrastructure.input.rest.mapper.DishRestMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Creates a dish. Authentication is added in a later story;
 * until then the security filter permits this call.
 */
@RestController
@Tag(name = "Dishes")
final class DishController {

    private final CreateDishHandler handler;
    private final DishRestMapper mapper = new DishRestMapper();

    DishController(CreateDishHandler handler) {
        this.handler = handler;
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
        Dish dish = handler.create(mapper.toNewDish(request));
        URI location = URI.create("/dishes/" + dish.id());
        return ResponseEntity.created(location).body(mapper.toResponse(dish));
    }
}

package com.foodcourt.plaza.infrastructure.input.rest;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.foodcourt.plaza.application.handler.CreateRestaurantHandler;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.infrastructure.input.rest.dto.CreateRestaurantRequest;
import com.foodcourt.plaza.infrastructure.input.rest.dto.RestaurantResponse;
import com.foodcourt.plaza.infrastructure.input.rest.mapper.RestaurantRestMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Creates a restaurant. The class is not final: method security needs a CGLIB subclass.
 */
@RestController
@Tag(name = "Restaurants")
@SecurityRequirement(name = "bearerAuth")
class RestaurantController {

    private final CreateRestaurantHandler handler;
    private final RestaurantRestMapper mapper = new RestaurantRestMapper();

    RestaurantController(CreateRestaurantHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/restaurants")
    @Operation(summary = "Create a restaurant")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Restaurant created"),
            @ApiResponse(responseCode = "400", description = "Invalid body or restaurant name"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
            @ApiResponse(responseCode = "403", description = "The caller is not an administrator, or the account is not an owner"),
            @ApiResponse(responseCode = "404", description = "No account exists for this id"),
            @ApiResponse(responseCode = "409", description = "NIT already registered"),
            @ApiResponse(responseCode = "503", description = "The user service is unavailable")
    })
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<RestaurantResponse> create(@Valid @RequestBody CreateRestaurantRequest request) {
        Restaurant restaurant = handler.create(mapper.toNewRestaurant(request));
        URI location = URI.create("/restaurants/" + restaurant.id());
        return ResponseEntity.created(location).body(mapper.toResponse(restaurant));
    }
}

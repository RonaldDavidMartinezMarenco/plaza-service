package com.foodcourt.plaza.infrastructure.input.rest;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foodcourt.plaza.application.handler.CreateDishHandler;
import com.foodcourt.plaza.application.handler.ListRestaurantDishesHandler;
import com.foodcourt.plaza.application.handler.SetDishActiveHandler;
import com.foodcourt.plaza.application.handler.UpdateDishHandler;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.DishSummary;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.infrastructure.input.rest.dto.CreateDishRequest;
import com.foodcourt.plaza.infrastructure.input.rest.dto.DishResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.DishSummaryResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.PageResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.SetDishActiveRequest;
import com.foodcourt.plaza.infrastructure.input.rest.dto.UpdateDishRequest;
import com.foodcourt.plaza.infrastructure.input.rest.mapper.DishRestMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Creates, updates, and enables or disables a dish, and lists the active menu of a restaurant.
 * Writing requires the owner of that restaurant. Listing requires a customer.
 * The class is not final: {@code @Validated} and method security need a CGLIB subclass.
 */
@RestController
@Validated
@Tag(name = "Dishes")
@SecurityRequirement(name = "bearerAuth")
class DishController {

    private final CreateDishHandler createDish;
    private final UpdateDishHandler updateDish;
    private final SetDishActiveHandler setDishActive;
    private final ListRestaurantDishesHandler listRestaurantDishes;
    private final DishRestMapper mapper = new DishRestMapper();

    DishController(
            CreateDishHandler createDish,
            UpdateDishHandler updateDish,
            SetDishActiveHandler setDishActive,
            ListRestaurantDishesHandler listRestaurantDishes) {
        this.createDish = createDish;
        this.updateDish = updateDish;
        this.setDishActive = setDishActive;
        this.listRestaurantDishes = listRestaurantDishes;
    }

    @GetMapping("/restaurants/{restaurantId}/dishes")
    @Operation(
            summary = "List the active dishes of a restaurant",
            description = "Order by name, then by id. The page index starts at zero. "
                    + "Omit category to list every active dish. A sort parameter is ignored. "
                    + "Inactive dishes are left out of the page and of the totals.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "A page of active dishes", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(ref = "#/components/schemas/PageResponseDishSummaryResponse"),
                    examples = @ExampleObject(name = "Carnes at La Parrilla", value = """
                            {
                              "items": [
                                {
                                  "id": 1,
                                  "restaurantId": 1,
                                  "name": "Churrasco",
                                  "price": 45000,
                                  "description": "Corte de res de 400 g a la parrilla con papas criollas y chimichurri.",
                                  "imageUrl": "https://images.example.com/dishes/churrasco.jpg",
                                  "category": "Carnes"
                                },
                                {
                                  "id": 2,
                                  "restaurantId": 1,
                                  "name": "Costillas BBQ",
                                  "price": 38000,
                                  "description": "Costillas de cerdo en salsa barbacoa con mazorca asada.",
                                  "imageUrl": "https://images.example.com/dishes/costillas-bbq.jpg",
                                  "category": "Carnes"
                                }
                              ],
                              "page": 0,
                              "size": 10,
                              "totalItems": 2,
                              "totalPages": 1
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Invalid restaurant id, page, size, or category"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
            @ApiResponse(responseCode = "403", description = "The caller is not a customer"),
            @ApiResponse(responseCode = "404", description = "No restaurant exists for this id")
    })
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PageResponse<DishSummaryResponse>> list(
            @Parameter(description = "La Parrilla de Carlos after the seed", example = "1")
            @PathVariable @Positive long restaurantId,
            @Parameter(description = "Page index, starting at zero", example = "0")
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @Parameter(description = "Dishes per page, from 1 to 50", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(PageQuery.MAX_SIZE) int size,
            @Parameter(
                    description = "Stored category. Omit it to list every active dish. "
                            + "A value that is not a category shape is rejected by the service.",
                    example = "Carnes")
            @RequestParam(required = false)
            @Size(max = 50)
            String category) {
        PageResult<DishSummary> result = listRestaurantDishes.list(
                restaurantId, mapper.toPageQuery(page, size), category);
        return ResponseEntity.ok(mapper.toSummaryPage(result));
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
    public ResponseEntity<DishResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateDishRequest.class),
                    examples = @ExampleObject(name = "New dish at La Parrilla", value = """
                            {
                              "name": "Bandeja paisa",
                              "price": 36000,
                              "description": "Frijoles, arroz, carne molida, chicharrón, huevo y aguacate.",
                              "imageUrl": "https://images.example.com/dishes/bandeja-paisa.jpg",
                              "category": "Platos fuertes",
                              "restaurantId": 1
                            }
                            """)))
            @Valid @RequestBody CreateDishRequest request) {
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
            @Parameter(description = "Costillas BBQ after the seed", example = "2")
            @PathVariable @Positive long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UpdateDishRequest.class),
                    examples = @ExampleObject(name = "Costillas BBQ", value = """
                            {
                              "price": 38000,
                              "description": "Costillas de cerdo en salsa barbacoa con mazorca asada."
                            }
                            """)))
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
            @Parameter(description = "Costillas BBQ after the seed", example = "2")
            @PathVariable @Positive long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SetDishActiveRequest.class),
                    examples = @ExampleObject(name = "Disable", value = """
                            {
                              "active": false
                            }
                            """)))
            @Valid @RequestBody SetDishActiveRequest request) {
        Dish dish = setDishActive.setActive(id, request.active());
        return ResponseEntity.ok(mapper.toResponse(dish));
    }
}

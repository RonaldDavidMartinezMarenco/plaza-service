package com.foodcourt.plaza.infrastructure.input.rest;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foodcourt.plaza.application.handler.CreateRestaurantHandler;
import com.foodcourt.plaza.application.handler.FindRestaurantHandler;
import com.foodcourt.plaza.application.handler.ListRestaurantsHandler;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.RestaurantSummary;
import com.foodcourt.plaza.infrastructure.input.rest.dto.CreateRestaurantRequest;
import com.foodcourt.plaza.infrastructure.input.rest.dto.PageResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.RestaurantOwnershipResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.RestaurantResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.RestaurantSummaryResponse;
import com.foodcourt.plaza.infrastructure.input.rest.mapper.RestaurantRestMapper;

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

/**
 * Creates a restaurant, lists them for a customer, and reports who owns one.
 * The class is not final: {@code @Validated} and method security need a CGLIB subclass.
 */
@RestController
@Validated
@Tag(name = "Restaurants")
@SecurityRequirement(name = "bearerAuth")
class RestaurantController {

    private final CreateRestaurantHandler createRestaurant;
    private final FindRestaurantHandler findRestaurant;
    private final ListRestaurantsHandler listRestaurants;
    private final RestaurantRestMapper mapper = new RestaurantRestMapper();

    RestaurantController(
            CreateRestaurantHandler createRestaurant,
            FindRestaurantHandler findRestaurant,
            ListRestaurantsHandler listRestaurants) {
        this.createRestaurant = createRestaurant;
        this.findRestaurant = findRestaurant;
        this.listRestaurants = listRestaurants;
    }

    @GetMapping("/restaurants")
    @Operation(
            summary = "List restaurants",
            description = "Alphabetical order by name using Spanish collation, then by id. "
                    + "The page index starts at zero. A sort parameter is ignored.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "A page of restaurants", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(ref = "#/components/schemas/PageResponseRestaurantSummaryResponse"),
                    examples = @ExampleObject(name = "First page after the seed", value = """
                            {
                              "items": [
                                {
                                  "id": 8,
                                  "name": "7 Mares Cevichería",
                                  "logoUrl": "https://images.example.com/logos/7-mares.png"
                                }
                              ],
                              "page": 0,
                              "size": 10,
                              "totalItems": 23,
                              "totalPages": 3
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Invalid page or size"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
            @ApiResponse(responseCode = "403", description = "The caller is not a customer")
    })
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PageResponse<RestaurantSummaryResponse>> list(
            @Parameter(description = "Page index, starting at zero", example = "0")
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @Parameter(description = "Restaurants per page, from 1 to 50", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(PageQuery.MAX_SIZE) int size) {
        PageResult<RestaurantSummary> result = listRestaurants.list(mapper.toPageQuery(page, size));
        return ResponseEntity.ok(mapper.toSummaryPage(result));
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
    public ResponseEntity<RestaurantResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateRestaurantRequest.class),
                    examples = @ExampleObject(name = "New restaurant for Andrés", value = """
                            {
                              "name": "Arepa y Mas",
                              "nit": "900100004",
                              "address": "Calle 12 # 8-40, Local 103",
                              "phone": "+576015550104",
                              "logoUrl": "https://images.example.com/logos/arepa.png",
                              "ownerId": 4
                            }
                            """)))
            @Valid @RequestBody CreateRestaurantRequest request) {
        Restaurant restaurant = createRestaurant.create(mapper.toNewRestaurant(request));
        URI location = URI.create("/restaurants/" + restaurant.id());
        return ResponseEntity.created(location).body(mapper.toResponse(restaurant));
    }

    @GetMapping("/restaurants/{id}")
    @Operation(summary = "Find who owns a restaurant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restaurant id and owner id"),
            @ApiResponse(responseCode = "400", description = "Invalid id"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
            @ApiResponse(responseCode = "403", description = "The caller is not an owner or an administrator"),
            @ApiResponse(responseCode = "404", description = "No restaurant exists for this id")
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMINISTRATOR')")
    public ResponseEntity<RestaurantOwnershipResponse> find(
            @Parameter(description = "La Parrilla de Carlos after the seed", example = "1")
            @PathVariable @Positive long id) {
        return ResponseEntity.ok(mapper.toOwnershipResponse(findRestaurant.find(id)));
    }
}

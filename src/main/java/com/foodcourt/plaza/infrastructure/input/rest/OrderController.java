package com.foodcourt.plaza.infrastructure.input.rest;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.foodcourt.plaza.application.handler.PlaceOrderHandler;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.infrastructure.input.rest.dto.OrderResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.PlaceOrderRequest;
import com.foodcourt.plaza.infrastructure.input.rest.mapper.OrderRestMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Places an order for the authenticated customer.
 * The class is not final: method security needs a CGLIB subclass.
 */
@RestController
@Tag(name = "Orders")
@SecurityRequirement(name = "bearerAuth")
class OrderController {

    private final PlaceOrderHandler placeOrder;
    private final OrderRestMapper mapper = new OrderRestMapper();

    OrderController(PlaceOrderHandler placeOrder) {
        this.placeOrder = placeOrder;
    }

    @PostMapping("/orders")
    @Operation(
            summary = "Place an order",
            description = "The customer comes from the access token. The order is stored as PENDING. "
                    + "Every dish must be active and belong to the restaurant. "
                    + "A customer with an order in PENDING, IN_PREPARATION, or READY cannot place another.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order placed", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = OrderResponse.class),
                    examples = @ExampleObject(name = "Churrasco and limonada", value = """
                            {
                              "id": 1,
                              "customerId": 9,
                              "restaurantId": 1,
                              "status": "PENDING",
                              "assignedEmployeeId": null,
                              "lines": [
                                { "dishId": 1, "quantity": 1 },
                                { "dishId": 4, "quantity": 2 }
                              ]
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Invalid body, quantity, or a repeated dish"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
            @ApiResponse(responseCode = "403", description = "The caller is not a customer"),
            @ApiResponse(responseCode = "404", description = "Restaurant, dish, or customer account does not exist"),
            @ApiResponse(responseCode = "409", description = "An order is already in progress, or a dish cannot be ordered"),
            @ApiResponse(responseCode = "503", description = "The user service is unavailable")
    })
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> place(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PlaceOrderRequest.class),
                    examples = @ExampleObject(name = "La Parrilla de Carlos", value = """
                            {
                              "restaurantId": 1,
                              "lines": [
                                { "dishId": 1, "quantity": 1 },
                                { "dishId": 4, "quantity": 2 }
                              ]
                            }
                            """)))
            @Valid @RequestBody PlaceOrderRequest request) {
        Order order = placeOrder.place(mapper.toDraft(request));
        URI location = URI.create("/orders/" + order.id());
        return ResponseEntity.created(location).body(mapper.toResponse(order));
    }
}

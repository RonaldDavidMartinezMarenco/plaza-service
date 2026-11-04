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

import com.foodcourt.plaza.application.handler.AssignOrderHandler;
import com.foodcourt.plaza.application.handler.ListRestaurantOrdersHandler;
import com.foodcourt.plaza.application.handler.PlaceOrderHandler;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.infrastructure.input.rest.dto.OrderResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.PageResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.PlaceOrderRequest;
import com.foodcourt.plaza.infrastructure.input.rest.mapper.OrderRestMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Places an order for a customer, lists orders for an employee, and lets that employee take one.
 * The class is not final: {@code @Validated} and method security need a CGLIB subclass.
 */
@RestController
@Validated
@Tag(name = "Orders")
@SecurityRequirement(name = "bearerAuth")
class OrderController {

    private final PlaceOrderHandler placeOrder;
    private final ListRestaurantOrdersHandler listOrders;
    private final AssignOrderHandler assignOrder;
    private final OrderRestMapper mapper = new OrderRestMapper();

    OrderController(
            PlaceOrderHandler placeOrder,
            ListRestaurantOrdersHandler listOrders,
            AssignOrderHandler assignOrder) {
        this.placeOrder = placeOrder;
        this.listOrders = listOrders;
        this.assignOrder = assignOrder;
    }

    @PostMapping("/orders/{orderId}/assignment")
    @Operation(
            summary = "Assign the order and start preparation",
            description = "The employee comes from the access token and must belong to the order's restaurant. "
                    + "The order must be PENDING and unassigned. It becomes IN_PREPARATION. "
                    + "A second call, another employee, and any other status are rejected. pinHash is omitted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order assigned", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = OrderResponse.class),
                    examples = @ExampleObject(name = "Camila's order taken by Pedro", value = """
                            {
                              "id": 1,
                              "customerId": 9,
                              "restaurantId": 1,
                              "status": "IN_PREPARATION",
                              "assignedEmployeeId": 5,
                              "lines": [
                                { "dishId": 1, "quantity": 1 },
                                { "dishId": 4, "quantity": 2 }
                              ]
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "The order id is not a positive integer"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
            @ApiResponse(responseCode = "403", description = "The caller is not an employee of a restaurant"),
            @ApiResponse(responseCode = "404", description = "The order, the employee account, or its restaurant does not exist"),
            @ApiResponse(responseCode = "409", description = "The order is not a pending unassigned order"),
            @ApiResponse(responseCode = "503", description = "The user service is unavailable")
    })
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<OrderResponse> assign(
            @Parameter(description = "Camila's order at La Parrilla after the seed", example = "1")
            @PathVariable @Positive long orderId) {
        return ResponseEntity.ok(mapper.toResponse(assignOrder.assign(orderId)));
    }

    @GetMapping("/orders")
    @Operation(
            summary = "List orders of the employee's restaurant",
            description = "The restaurant comes from the employee account, not from the request. "
                    + "status is required and must be exactly one of PENDING, IN_PREPARATION, READY, "
                    + "DELIVERED, or CANCELLED. The page is ordered by id. "
                    + "A sort parameter and a restaurant id are ignored. pinHash is omitted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "A page of orders", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(ref = "#/components/schemas/PageResponseOrderResponse"),
                    examples = @ExampleObject(name = "First pending orders at La Parrilla", value = """
                            {
                              "items": [
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
                                },
                                {
                                  "id": 2,
                                  "customerId": 12,
                                  "restaurantId": 1,
                                  "status": "PENDING",
                                  "assignedEmployeeId": null,
                                  "lines": [
                                    { "dishId": 1, "quantity": 2 }
                                  ]
                                }
                              ],
                              "page": 0,
                              "size": 2,
                              "totalItems": 12,
                              "totalPages": 6
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Missing, repeated, or unknown status, or an invalid page or size"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
            @ApiResponse(responseCode = "403", description = "The caller is not an employee of a restaurant"),
            @ApiResponse(responseCode = "404", description = "The employee account or its restaurant does not exist"),
            @ApiResponse(responseCode = "503", description = "The user service is unavailable")
    })
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<PageResponse<OrderResponse>> list(
            @Parameter(
                    description = "Exactly one status",
                    schema = @Schema(implementation = OrderStatus.class),
                    example = "PENDING")
            @RequestParam String status,
            @Parameter(description = "Page index, starting at zero", example = "0")
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @Parameter(description = "Orders per page, from 1 to 50", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(PageQuery.MAX_SIZE) int size,
            @Parameter(hidden = true) HttpServletRequest request) {
        OrderStatus.requireOne(request.getParameterValues("status"));
        PageResult<Order> result = listOrders.list(
                mapper.toListQuery(OrderStatus.parseExact(status), page, size));
        return ResponseEntity.ok(mapper.toPage(result));
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

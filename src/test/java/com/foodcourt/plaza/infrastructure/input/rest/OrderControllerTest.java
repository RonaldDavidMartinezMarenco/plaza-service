package com.foodcourt.plaza.infrastructure.input.rest;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.foodcourt.plaza.application.handler.AssignOrderHandler;
import com.foodcourt.plaza.application.handler.ListRestaurantOrdersHandler;
import com.foodcourt.plaza.application.handler.MarkOrderReadyHandler;
import com.foodcourt.plaza.application.handler.PlaceOrderHandler;
import com.foodcourt.plaza.domain.exception.ActiveOrderExists;
import com.foodcourt.plaza.domain.exception.CustomerNotFound;
import com.foodcourt.plaza.domain.exception.EmployeeNotFound;
import com.foodcourt.plaza.domain.exception.EmployeeRoleRequired;
import com.foodcourt.plaza.domain.exception.EmployeeWithoutRestaurant;
import com.foodcourt.plaza.domain.exception.CustomerRoleRequired;
import com.foodcourt.plaza.domain.exception.DishInactive;
import com.foodcourt.plaza.domain.exception.DishNotFound;
import com.foodcourt.plaza.domain.exception.DishRestaurantMismatch;
import com.foodcourt.plaza.domain.exception.DuplicateOrderLine;
import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;
import com.foodcourt.plaza.domain.exception.InvalidOrderStatus;
import com.foodcourt.plaza.domain.exception.OrderAlreadyAssigned;
import com.foodcourt.plaza.domain.exception.OrderNotAssignable;
import com.foodcourt.plaza.domain.exception.NotificationRejected;
import com.foodcourt.plaza.domain.exception.NotificationUnavailable;
import com.foodcourt.plaza.domain.exception.OrderNotFound;
import com.foodcourt.plaza.domain.exception.OrderNotReady;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.model.OrderListQuery;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.model.PlaceOrderDraft;
import com.foodcourt.plaza.infrastructure.config.JacksonConfig;
import com.foodcourt.plaza.infrastructure.exception.ApiExceptionHandler;
import com.foodcourt.plaza.infrastructure.security.SecurityConfig;
import com.foodcourt.plaza.infrastructure.security.SecurityProblemHandlers;

@WebMvcTest(OrderController.class)
@Import({ApiExceptionHandler.class, SecurityConfig.class, SecurityProblemHandlers.class, JacksonConfig.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceOrderHandler handler;

    @MockitoBean
    private ListRestaurantOrdersHandler listOrders;

    @MockitoBean
    private AssignOrderHandler assignOrder;

    @MockitoBean
    private MarkOrderReadyHandler markReady;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void placeReturnsThePendingOrder() throws Exception {
        when(handler.place(any(PlaceOrderDraft.class))).thenReturn(stored());

        place(validBody())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/orders/1"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerId").value(9))
                .andExpect(jsonPath("$.restaurantId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.assignedEmployeeId").value(nullValue()))
                .andExpect(jsonPath("$.pinHash").doesNotExist())
                .andExpect(jsonPath("$.lines[0].dishId").value(1))
                .andExpect(jsonPath("$.lines[0].quantity").value(1))
                .andExpect(jsonPath("$.lines[1].dishId").value(4))
                .andExpect(jsonPath("$.lines[1].quantity").value(2));

        org.mockito.ArgumentCaptor<PlaceOrderDraft> sent = org.mockito.ArgumentCaptor.forClass(PlaceOrderDraft.class);
        verify(handler).place(sent.capture());
        assertThatRestaurantAndLines(sent.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"restaurantId\":1,\"lines\":[{\"dishId\":1,\"quantity\":0}]}",
            "{\"restaurantId\":1,\"lines\":[{\"dishId\":1,\"quantity\":51}]}",
            "{\"restaurantId\":1,\"lines\":[{\"dishId\":1,\"quantity\":1.5}]}",
            "{\"restaurantId\":1,\"lines\":[]}",
            "{\"lines\":[{\"dishId\":1,\"quantity\":1}]}",
            "{\"restaurantId\":0,\"lines\":[{\"dishId\":1,\"quantity\":1}]}",
            "{\"restaurantId\":1,\"status\":\"DELIVERED\",\"lines\":[{\"dishId\":1,\"quantity\":1}]}",
            "{\"restaurantId\":1,\"customerId\":9,\"lines\":[{\"dishId\":1,\"quantity\":1}]}",
            "not-json"
    })
    void placeRejectsAnInvalidBody(String body) throws Exception {
        place(body).andExpect(status().isBadRequest());
        verify(handler, never()).place(any());
    }

    @Test
    void placeRejectsABodyThatIsNotJson() throws Exception {
        mockMvc.perform(post("/orders").with(customer()).contentType(MediaType.TEXT_PLAIN).content("hello"))
                .andExpect(status().isUnsupportedMediaType());
        verifyNoInteractions(handler);
    }

    @Test
    void placeRejectsMoreThanTwentyLines() throws Exception {
        place(twentyOneLines()).andExpect(status().isBadRequest());
        verify(handler, never()).place(any());
    }

    @Test
    void placeRejectsARepeatedDish() throws Exception {
        when(handler.place(any(PlaceOrderDraft.class))).thenThrow(new DuplicateOrderLine());

        place("""
                {"restaurantId":1,"lines":[{"dishId":1,"quantity":1},{"dishId":1,"quantity":2}]}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Duplicate dish"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void placeRejectsAMissingToken() throws Exception {
        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Authentication is required"));
        verifyNoInteractions(handler);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_ADMINISTRATOR", "ROLE_OWNER", "ROLE_EMPLOYEE"})
    void placeRejectsACallerThatIsNotACustomer(String authority) throws Exception {
        mockMvc.perform(post("/orders")
                        .with(jwt().authorities(new SimpleGrantedAuthority(authority)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("You do not have permission for this action"));
        verifyNoInteractions(handler);
    }

    @Test
    void placeRejectsAnAccountThatIsNotACustomer() throws Exception {
        when(handler.place(any(PlaceOrderDraft.class))).thenThrow(new CustomerRoleRequired());

        place(validBody())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Customer role required"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void placeRejectsAMissingRestaurant() throws Exception {
        when(handler.place(any(PlaceOrderDraft.class))).thenThrow(new RestaurantNotFound());

        place(validBody())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Restaurant not found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void placeRejectsAMissingDish() throws Exception {
        when(handler.place(any(PlaceOrderDraft.class))).thenThrow(new DishNotFound());

        place(validBody())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Dish not found"));
    }

    @Test
    void placeRejectsAMissingCustomer() throws Exception {
        when(handler.place(any(PlaceOrderDraft.class))).thenThrow(new CustomerNotFound());

        place(validBody())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Customer not found"));
    }

    @Test
    void placeRejectsAnOrderAlreadyInProgress() throws Exception {
        when(handler.place(any(PlaceOrderDraft.class))).thenThrow(new ActiveOrderExists());

        place(validBody())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Active order exists"))
                .andExpect(jsonPath("$.detail").value("The customer already has an order in progress"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void placeRejectsAnInactiveDish() throws Exception {
        when(handler.place(any(PlaceOrderDraft.class))).thenThrow(new DishInactive());

        place(validBody())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Dish inactive"));
    }

    @Test
    void placeRejectsADishFromAnotherRestaurant() throws Exception {
        when(handler.place(any(PlaceOrderDraft.class))).thenThrow(new DishRestaurantMismatch());

        place(validBody())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Dish from another restaurant"));
    }

    @Test
    void placeRejectsWhenTheUserServiceIsDown() throws Exception {
        when(handler.place(any(PlaceOrderDraft.class))).thenThrow(new OwnerDirectoryUnavailable());

        place(validBody())
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("User service unavailable"))
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void listReturnsTheEmployeePageWithoutThePin() throws Exception {
        when(listOrders.list(any(OrderListQuery.class))).thenReturn(new PageResult<>(List.of(stored()), 0, 10, 12));

        list("?status=PENDING&page=0&size=10")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalItems").value(12))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].restaurantId").value(1))
                .andExpect(jsonPath("$.items[0].status").value("PENDING"))
                .andExpect(jsonPath("$.items[0].assignedEmployeeId").value(nullValue()))
                .andExpect(jsonPath("$.items[0].pinHash").doesNotExist())
                .andExpect(jsonPath("$.items[0].lines[0].dishId").value(1))
                .andExpect(jsonPath("$.items[0].lines[1].quantity").value(2));

        org.mockito.ArgumentCaptor<OrderListQuery> sent = org.mockito.ArgumentCaptor.forClass(OrderListQuery.class);
        verify(listOrders).list(sent.capture());
        org.assertj.core.api.Assertions.assertThat(sent.getValue())
                .isEqualTo(new OrderListQuery(new PageQuery(0, 10), OrderStatus.PENDING));
    }

    @Test
    void listIgnoresSortAndRestaurantId() throws Exception {
        when(listOrders.list(any(OrderListQuery.class))).thenReturn(new PageResult<>(List.of(), 0, 10, 0));

        list("?status=READY&sort=id,desc&restaurantId=2").andExpect(status().isOk());

        org.mockito.ArgumentCaptor<OrderListQuery> sent = org.mockito.ArgumentCaptor.forClass(OrderListQuery.class);
        verify(listOrders).list(sent.capture());
        org.assertj.core.api.Assertions.assertThat(sent.getValue())
                .isEqualTo(new OrderListQuery(new PageQuery(0, 10), OrderStatus.READY));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "?status=", "?status=pending", "?status=PENDIENTE", "?status=PENDING&status=READY"})
    void listRejectsAMissingOrUnknownStatus(String query) throws Exception {
        list(query)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(listOrders);
    }

    @ParameterizedTest
    @ValueSource(strings = {"?status=PENDING&page=-1", "?status=PENDING&size=0", "?status=PENDING&size=51"})
    void listRejectsAnInvalidPage(String query) throws Exception {
        list(query).andExpect(status().isBadRequest());
        verifyNoInteractions(listOrders);
    }

    @Test
    void listRejectsAMissingToken() throws Exception {
        mockMvc.perform(get("/orders?status=PENDING"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Authentication is required"));
        verifyNoInteractions(listOrders);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_ADMINISTRATOR", "ROLE_OWNER", "ROLE_CUSTOMER"})
    void listRejectsACallerThatIsNotAnEmployee(String authority) throws Exception {
        mockMvc.perform(get("/orders?status=PENDIENTE")
                        .with(jwt().authorities(new SimpleGrantedAuthority(authority))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("You do not have permission for this action"));
        verifyNoInteractions(listOrders);
    }

    @Test
    void listRejectsAMissingEmployee() throws Exception {
        when(listOrders.list(any(OrderListQuery.class))).thenThrow(new EmployeeNotFound());

        list("?status=PENDING")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Employee not found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listRejectsAMissingRestaurant() throws Exception {
        when(listOrders.list(any(OrderListQuery.class))).thenThrow(new RestaurantNotFound());

        list("?status=PENDING")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Restaurant not found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listRejectsAnAccountThatIsNotAnEmployee() throws Exception {
        when(listOrders.list(any(OrderListQuery.class))).thenThrow(new EmployeeRoleRequired());

        list("?status=PENDING")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Employee role required"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void listRejectsAnEmployeeWithoutARestaurant() throws Exception {
        when(listOrders.list(any(OrderListQuery.class))).thenThrow(new EmployeeWithoutRestaurant());

        list("?status=PENDING")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Employee without restaurant"))
                .andExpect(jsonPath("$.detail").value("The employee is not assigned to a restaurant"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void listRejectsWhenTheUserServiceIsDown() throws Exception {
        when(listOrders.list(any(OrderListQuery.class))).thenThrow(new OwnerDirectoryUnavailable());

        list("?status=PENDING")
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("User service unavailable"))
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void listReportsAnInvalidStatusFromTheUseCase() throws Exception {
        when(listOrders.list(any(OrderListQuery.class))).thenThrow(new InvalidOrderStatus());

        list("?status=PENDING")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid order status"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void assignReturnsTheOrderInPreparation() throws Exception {
        when(assignOrder.assign(1L)).thenReturn(claimed());

        assign("/orders/1/assignment")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerId").value(9))
                .andExpect(jsonPath("$.restaurantId").value(1))
                .andExpect(jsonPath("$.status").value("IN_PREPARATION"))
                .andExpect(jsonPath("$.assignedEmployeeId").value(5))
                .andExpect(jsonPath("$.pinHash").doesNotExist())
                .andExpect(jsonPath("$.lines[0].dishId").value(1))
                .andExpect(jsonPath("$.lines[0].quantity").value(1))
                .andExpect(jsonPath("$.lines[1].dishId").value(4))
                .andExpect(jsonPath("$.lines[1].quantity").value(2));
        verify(assignOrder).assign(1L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/orders/0/assignment", "/orders/-1/assignment", "/orders/abc/assignment"})
    void assignRejectsAnInvalidOrderId(String path) throws Exception {
        assign(path).andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(assignOrder);
    }

    @Test
    void assignRejectsAMissingToken() throws Exception {
        mockMvc.perform(post("/orders/1/assignment"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Authentication is required"));
        verifyNoInteractions(assignOrder);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_ADMINISTRATOR", "ROLE_OWNER", "ROLE_CUSTOMER"})
    void assignRejectsACallerThatIsNotAnEmployee(String authority) throws Exception {
        mockMvc.perform(post("/orders/1/assignment")
                        .with(jwt().authorities(new SimpleGrantedAuthority(authority))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("You do not have permission for this action"));
        verifyNoInteractions(assignOrder);
    }

    @Test
    void assignRejectsAMissingOrder() throws Exception {
        when(assignOrder.assign(1L)).thenThrow(new OrderNotFound());

        assign("/orders/1/assignment")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Order not found"))
                .andExpect(jsonPath("$.detail").value("No order exists for this id"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void assignRejectsAMissingEmployee() throws Exception {
        when(assignOrder.assign(1L)).thenThrow(new EmployeeNotFound());

        assign("/orders/1/assignment")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Employee not found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void assignRejectsAMissingRestaurant() throws Exception {
        when(assignOrder.assign(1L)).thenThrow(new RestaurantNotFound());

        assign("/orders/1/assignment")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Restaurant not found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void assignRejectsAnAccountThatIsNotAnEmployee() throws Exception {
        when(assignOrder.assign(1L)).thenThrow(new EmployeeRoleRequired());

        assign("/orders/1/assignment")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Employee role required"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void assignRejectsAnEmployeeWithoutARestaurant() throws Exception {
        when(assignOrder.assign(1L)).thenThrow(new EmployeeWithoutRestaurant());

        assign("/orders/1/assignment")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Employee without restaurant"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void assignRejectsAnOrderThatIsNotPending() throws Exception {
        when(assignOrder.assign(1L)).thenThrow(new OrderNotAssignable(OrderStatus.READY));

        assign("/orders/1/assignment")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Order cannot be assigned"))
                .andExpect(jsonPath("$.detail").value("The order is READY and cannot be assigned"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void assignRejectsAnOrderThatAlreadyHasAnEmployee() throws Exception {
        when(assignOrder.assign(1L)).thenThrow(new OrderAlreadyAssigned());

        assign("/orders/1/assignment")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Order already assigned"))
                .andExpect(jsonPath("$.detail").value("The order already has an assigned employee"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void assignRejectsWhenTheUserServiceIsDown() throws Exception {
        when(assignOrder.assign(1L)).thenThrow(new OwnerDirectoryUnavailable());

        assign("/orders/1/assignment")
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("User service unavailable"))
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void readyReturnsTheOrderWithoutThePin() throws Exception {
        when(markReady.markReady(1L)).thenReturn(new Order(
                1L, 9L, 1L, OrderStatus.READY, 5L, "hash", List.of(new OrderLine(1L, 1), new OrderLine(4L, 2))));

        assign("/orders/1/ready")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.assignedEmployeeId").value(5))
                .andExpect(jsonPath("$.pin").doesNotExist())
                .andExpect(jsonPath("$.pinHash").doesNotExist());
    }

    @Test
    void readyRejectsAnOrderThatCannotChange() throws Exception {
        when(markReady.markReady(1L)).thenThrow(new OrderNotReady(OrderStatus.PENDING));

        assign("/orders/1/ready")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("The order is PENDING and cannot be marked ready"));
    }

    @Test
    void readyReportsARejectedMessage() throws Exception {
        when(markReady.markReady(1L)).thenThrow(new NotificationRejected());

        assign("/orders/1/ready")
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.detail").value("The order-ready message was rejected"));
    }

    @Test
    void readyReportsAnUnavailableMessagingService() throws Exception {
        when(markReady.markReady(1L)).thenThrow(new NotificationUnavailable());

        assign("/orders/1/ready")
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    private ResultActions assign(String path) throws Exception {
        return mockMvc.perform(post(path).with(employee()));
    }

    private static Order claimed() {
        return new Order(
                1L,
                9L,
                1L,
                OrderStatus.IN_PREPARATION,
                5L,
                null,
                List.of(new OrderLine(1L, 1), new OrderLine(4L, 2)));
    }

    private ResultActions list(String query) throws Exception {
        return mockMvc.perform(get("/orders" + query).with(employee()));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor employee() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
    }

    private static void assertThatRestaurantAndLines(PlaceOrderDraft draft) {
        org.assertj.core.api.Assertions.assertThat(draft.restaurantId()).isEqualTo(1L);
        org.assertj.core.api.Assertions.assertThat(draft.lines()).containsExactly(
                new PlaceOrderDraft.OrderLineDraft(1L, 1),
                new PlaceOrderDraft.OrderLineDraft(4L, 2));
    }

    private ResultActions place(String body) throws Exception {
        return mockMvc.perform(post("/orders")
                .with(customer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor customer() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    private static String validBody() {
        return """
                {
                  "restaurantId": 1,
                  "lines": [
                    { "dishId": 1, "quantity": 1 },
                    { "dishId": 4, "quantity": 2 }
                  ]
                }
                """;
    }

    private static Order stored() {
        return new Order(
                1L,
                9L,
                1L,
                OrderStatus.PENDING,
                null,
                null,
                List.of(new OrderLine(1L, 1), new OrderLine(4L, 2)));
    }

    private static String twentyOneLines() {
        String lines = java.util.stream.IntStream.rangeClosed(1, 21)
                .mapToObj(id -> "{\"dishId\":" + id + ",\"quantity\":1}")
                .collect(java.util.stream.Collectors.joining(","));
        return "{\"restaurantId\":1,\"lines\":[" + lines + "]}";
    }
}

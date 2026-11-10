package com.foodcourt.plaza.infrastructure.exception;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.foodcourt.plaza.domain.exception.AccessDenied;
import com.foodcourt.plaza.domain.exception.AccountLookupRejected;
import com.foodcourt.plaza.domain.exception.ActiveOrderExists;
import com.foodcourt.plaza.domain.exception.AuthenticationRequired;
import com.foodcourt.plaza.domain.exception.CustomerNotFound;
import com.foodcourt.plaza.domain.exception.CustomerRoleRequired;
import com.foodcourt.plaza.domain.exception.DishInactive;
import com.foodcourt.plaza.domain.exception.DishNotFound;
import com.foodcourt.plaza.domain.exception.DishRestaurantMismatch;
import com.foodcourt.plaza.domain.exception.DuplicateOrderLine;
import com.foodcourt.plaza.domain.exception.EmployeeNotFound;
import com.foodcourt.plaza.domain.exception.EmployeeRoleRequired;
import com.foodcourt.plaza.domain.exception.EmployeeWithoutRestaurant;
import com.foodcourt.plaza.domain.exception.EmptyOrder;
import com.foodcourt.plaza.domain.exception.InvalidDishDescription;
import com.foodcourt.plaza.domain.exception.InvalidDishPrice;
import com.foodcourt.plaza.domain.exception.InvalidDishText;
import com.foodcourt.plaza.domain.exception.InvalidOrderQuantity;
import com.foodcourt.plaza.domain.exception.InvalidOrderStatus;
import com.foodcourt.plaza.domain.exception.InvalidPageRequest;
import com.foodcourt.plaza.domain.exception.InvalidRestaurantAddress;
import com.foodcourt.plaza.domain.exception.InvalidRestaurantName;
import com.foodcourt.plaza.domain.exception.NitAlreadyRegistered;
import com.foodcourt.plaza.domain.exception.RestaurantNameAlreadyRegistered;
import com.foodcourt.plaza.domain.exception.OrderAlreadyAssigned;
import com.foodcourt.plaza.domain.exception.NotificationRejected;
import com.foodcourt.plaza.domain.exception.NotificationUnavailable;
import com.foodcourt.plaza.domain.exception.OrderAssignedToAnotherEmployee;
import com.foodcourt.plaza.domain.exception.OrderNotAssignable;
import com.foodcourt.plaza.domain.exception.OrderNotCancellable;
import com.foodcourt.plaza.domain.exception.OrderNotDeliverable;
import com.foodcourt.plaza.domain.exception.OrderNotFound;
import com.foodcourt.plaza.domain.exception.OrderNotReady;
import com.foodcourt.plaza.domain.exception.PinDoesNotMatch;
import com.foodcourt.plaza.domain.exception.TraceabilityRejected;
import com.foodcourt.plaza.domain.exception.TraceabilityUnavailable;
import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;
import com.foodcourt.plaza.domain.exception.OwnerNotFound;
import com.foodcourt.plaza.domain.exception.OwnerRoleRequired;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

/**
 * Maps domain failures to {@code application/problem+json}.
 * Bean Validation is left to Spring, which already returns HTTP 400.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AuthenticationRequired.class)
    ProblemDetail authenticationRequired(AuthenticationRequired exception, HttpServletRequest request) {
        return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", exception, request);
    }

    @ExceptionHandler(AccessDenied.class)
    ProblemDetail accessDenied(AccessDenied exception, HttpServletRequest request) {
        return problem(HttpStatus.FORBIDDEN, "Forbidden", exception, request);
    }

    @ExceptionHandler(AccountLookupRejected.class)
    ProblemDetail accountLookupRejected(AccountLookupRejected exception, HttpServletRequest request) {
        return problem(HttpStatus.FORBIDDEN, "Account lookup rejected", exception, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail invalidConstraint(ConstraintViolationException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", exception, request);
    }

    @ExceptionHandler(InvalidRestaurantName.class)
    ProblemDetail invalidName(InvalidRestaurantName exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid restaurant name", exception, request);
    }

    @ExceptionHandler(InvalidRestaurantAddress.class)
    ProblemDetail invalidAddress(InvalidRestaurantAddress exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid restaurant address", exception, request);
    }

    @ExceptionHandler(InvalidDishText.class)
    ProblemDetail invalidDishText(InvalidDishText exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid dish text", exception, request);
    }

    @ExceptionHandler(InvalidPageRequest.class)
    ProblemDetail invalidPage(InvalidPageRequest exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid page request", exception, request);
    }

    @ExceptionHandler(InvalidDishDescription.class)
    ProblemDetail invalidDishDescription(InvalidDishDescription exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid dish description", exception, request);
    }

    @ExceptionHandler(InvalidDishPrice.class)
    ProblemDetail invalidDishPrice(InvalidDishPrice exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid dish price", exception, request);
    }

    @ExceptionHandler(DishNotFound.class)
    ProblemDetail missingDish(DishNotFound exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Dish not found", exception, request);
    }

    @ExceptionHandler(RestaurantNotFound.class)
    ProblemDetail missingRestaurant(RestaurantNotFound exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Restaurant not found", exception, request);
    }

    @ExceptionHandler(OwnerRoleRequired.class)
    ProblemDetail wrongRole(OwnerRoleRequired exception, HttpServletRequest request) {
        return problem(HttpStatus.FORBIDDEN, "Owner role required", exception, request);
    }

    @ExceptionHandler(OwnerNotFound.class)
    ProblemDetail missingOwner(OwnerNotFound exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Owner not found", exception, request);
    }

    @ExceptionHandler(NitAlreadyRegistered.class)
    ProblemDetail nitTaken(NitAlreadyRegistered exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "NIT already registered", exception, request);
    }

    @ExceptionHandler(RestaurantNameAlreadyRegistered.class)
    ProblemDetail nameTaken(RestaurantNameAlreadyRegistered exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Restaurant name already registered", exception, request);
    }

    @ExceptionHandler(OwnerDirectoryUnavailable.class)
    ProblemDetail directoryDown(OwnerDirectoryUnavailable exception, HttpServletRequest request) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "User service unavailable", exception, request);
    }

    @ExceptionHandler(EmptyOrder.class)
    ProblemDetail emptyOrder(EmptyOrder exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Empty order", exception, request);
    }

    @ExceptionHandler(DuplicateOrderLine.class)
    ProblemDetail duplicateLine(DuplicateOrderLine exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Duplicate dish", exception, request);
    }

    @ExceptionHandler(InvalidOrderQuantity.class)
    ProblemDetail invalidQuantity(InvalidOrderQuantity exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid quantity", exception, request);
    }

    @ExceptionHandler(CustomerNotFound.class)
    ProblemDetail missingCustomer(CustomerNotFound exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Customer not found", exception, request);
    }

    @ExceptionHandler(CustomerRoleRequired.class)
    ProblemDetail customerRole(CustomerRoleRequired exception, HttpServletRequest request) {
        return problem(HttpStatus.FORBIDDEN, "Customer role required", exception, request);
    }

    @ExceptionHandler(DishInactive.class)
    ProblemDetail inactiveDish(DishInactive exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Dish inactive", exception, request);
    }

    @ExceptionHandler(DishRestaurantMismatch.class)
    ProblemDetail dishRestaurant(DishRestaurantMismatch exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Dish from another restaurant", exception, request);
    }

    @ExceptionHandler(ActiveOrderExists.class)
    ProblemDetail activeOrder(ActiveOrderExists exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Active order exists", exception, request);
    }

    @ExceptionHandler(InvalidOrderStatus.class)
    ProblemDetail invalidStatus(InvalidOrderStatus exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid order status", exception, request);
    }

    @ExceptionHandler(EmployeeNotFound.class)
    ProblemDetail missingEmployee(EmployeeNotFound exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Employee not found", exception, request);
    }

    @ExceptionHandler(EmployeeRoleRequired.class)
    ProblemDetail employeeRole(EmployeeRoleRequired exception, HttpServletRequest request) {
        return problem(HttpStatus.FORBIDDEN, "Employee role required", exception, request);
    }

    @ExceptionHandler(EmployeeWithoutRestaurant.class)
    ProblemDetail employeeWithoutRestaurant(EmployeeWithoutRestaurant exception, HttpServletRequest request) {
        return problem(HttpStatus.FORBIDDEN, "Employee without restaurant", exception, request);
    }

    @ExceptionHandler(OrderNotFound.class)
    ProblemDetail missingOrder(OrderNotFound exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Order not found", exception, request);
    }

    @ExceptionHandler(OrderNotAssignable.class)
    ProblemDetail orderNotAssignable(OrderNotAssignable exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Order cannot be assigned", exception, request);
    }

    @ExceptionHandler(OrderAlreadyAssigned.class)
    ProblemDetail orderAlreadyAssigned(OrderAlreadyAssigned exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Order already assigned", exception, request);
    }

    @ExceptionHandler(OrderNotReady.class)
    ProblemDetail orderNotReady(OrderNotReady exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Order cannot be marked ready", exception, request);
    }

    @ExceptionHandler(OrderAssignedToAnotherEmployee.class)
    ProblemDetail assignedToAnother(OrderAssignedToAnotherEmployee exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Order assigned to another employee", exception, request);
    }

    @ExceptionHandler(OrderNotDeliverable.class)
    ProblemDetail orderNotDeliverable(OrderNotDeliverable exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Order cannot be delivered", exception, request);
    }

    @ExceptionHandler(OrderNotCancellable.class)
    ProblemDetail orderNotCancellable(OrderNotCancellable exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Order cannot be cancelled", exception, request);
    }

    @ExceptionHandler(PinDoesNotMatch.class)
    ProblemDetail pinDoesNotMatch(PinDoesNotMatch exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Security PIN does not match", exception, request);
    }

    @ExceptionHandler(NotificationRejected.class)
    ProblemDetail notificationRejected(NotificationRejected exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_GATEWAY, "Message rejected", exception, request);
    }

    @ExceptionHandler(NotificationUnavailable.class)
    ProblemDetail notificationUnavailable(NotificationUnavailable exception, HttpServletRequest request) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Messaging service unavailable", exception, request);
    }

    @ExceptionHandler(TraceabilityRejected.class)
    ProblemDetail traceabilityRejected(TraceabilityRejected exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Trace rejected", exception, request);
    }

    @ExceptionHandler(TraceabilityUnavailable.class)
    ProblemDetail traceabilityUnavailable(TraceabilityUnavailable exception, HttpServletRequest request) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Traceability service unavailable", exception, request);
    }

    private static ProblemDetail problem(
            HttpStatus status,
            String title,
            RuntimeException exception,
            HttpServletRequest request) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        body.setTitle(title);
        body.setInstance(URI.create(request.getRequestURI()));
        return body;
    }
}

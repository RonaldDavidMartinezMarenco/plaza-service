package com.foodcourt.plaza.infrastructure.exception;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.foodcourt.plaza.domain.exception.InvalidDishPrice;
import com.foodcourt.plaza.domain.exception.InvalidDishText;
import com.foodcourt.plaza.domain.exception.InvalidRestaurantName;
import com.foodcourt.plaza.domain.exception.NitAlreadyRegistered;
import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;
import com.foodcourt.plaza.domain.exception.OwnerNotFound;
import com.foodcourt.plaza.domain.exception.OwnerRoleRequired;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Maps domain failures to {@code application/problem+json}.
 * Bean Validation is left to Spring, which already returns HTTP 400.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidRestaurantName.class)
    ProblemDetail invalidName(InvalidRestaurantName exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid restaurant name", exception, request);
    }

    @ExceptionHandler(InvalidDishText.class)
    ProblemDetail invalidDishText(InvalidDishText exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid dish text", exception, request);
    }

    @ExceptionHandler(InvalidDishPrice.class)
    ProblemDetail invalidDishPrice(InvalidDishPrice exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid dish price", exception, request);
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

    @ExceptionHandler(OwnerDirectoryUnavailable.class)
    ProblemDetail directoryDown(OwnerDirectoryUnavailable exception, HttpServletRequest request) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "User service unavailable", exception, request);
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

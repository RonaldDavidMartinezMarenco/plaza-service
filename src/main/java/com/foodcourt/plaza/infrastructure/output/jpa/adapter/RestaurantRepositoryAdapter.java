package com.foodcourt.plaza.infrastructure.output.jpa.adapter;

import java.sql.SQLException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodcourt.plaza.domain.exception.NitAlreadyRegistered;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;
import com.foodcourt.plaza.infrastructure.output.jpa.entity.RestaurantEntity;
import com.foodcourt.plaza.infrastructure.output.jpa.mapper.RestaurantJpaMapper;
import com.foodcourt.plaza.infrastructure.output.jpa.repository.RestaurantJpaRepository;

/**
 * JPA adapter for {@link RestaurantRepository}.
 * A concurrent insert can pass the exists check and still hit the unique index.
 * PostgreSQL reports that as SQLState {@code 23505}; the message cites {@code nit}.
 * The class is not final: {@code @Transactional} needs a CGLIB subclass.
 */
@Service
public class RestaurantRepositoryAdapter implements RestaurantRepository {

    /** PostgreSQL {@code unique_violation}. */
    static final String UNIQUE_VIOLATION = "23505";

    private final RestaurantJpaRepository restaurants;
    private final RestaurantJpaMapper mapper = new RestaurantJpaMapper();

    public RestaurantRepositoryAdapter(RestaurantJpaRepository restaurants) {
        this.restaurants = restaurants;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNit(String nit) {
        return restaurants.existsByNit(nit);
    }

    @Override
    @Transactional
    public Restaurant save(Restaurant restaurant) {
        try {
            RestaurantEntity stored = restaurants.saveAndFlush(mapper.toEntity(restaurant));
            return mapper.toDomain(stored);
        } catch (DataIntegrityViolationException failure) {
            throw translated(failure);
        }
    }

    private RuntimeException translated(DataIntegrityViolationException failure) {
        if (isUniqueNit(failure)) {
            return new NitAlreadyRegistered();
        }
        return failure;
    }

    private static boolean isUniqueNit(DataIntegrityViolationException failure) {
        return isUniqueViolation(failure) && mentionsNit(causeMessage(failure));
    }

    private static boolean isUniqueViolation(DataIntegrityViolationException failure) {
        Throwable cause = failure.getMostSpecificCause();
        if (cause instanceof SQLException sqlException) {
            return UNIQUE_VIOLATION.equals(sqlException.getSQLState());
        }
        return false;
    }

    private static String causeMessage(DataIntegrityViolationException failure) {
        return failure.getMostSpecificCause().getMessage();
    }

    private static boolean mentionsNit(String message) {
        return message != null && message.contains("nit");
    }
}

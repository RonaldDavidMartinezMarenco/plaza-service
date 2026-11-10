package com.foodcourt.plaza.infrastructure.output.client;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.foodcourt.plaza.domain.exception.AccountLookupRejected;
import com.foodcourt.plaza.domain.exception.CustomerNotFound;
import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;
import com.foodcourt.plaza.domain.spi.CustomerPhoneDirectory;

/**
 * Reads {@code GET /users/{id}/phone} from user-service.
 * HTTP 404 is a missing customer. HTTP 401 and 403 are a rejected lookup.
 */
@Service
public final class UserServiceCustomerPhoneDirectory implements CustomerPhoneDirectory {

    private final RestClient userService;

    public UserServiceCustomerPhoneDirectory(@Qualifier("userServiceClient") RestClient userService) {
        this.userService = userService;
    }

    @Override
    public String findPhone(long customerId) {
        try {
            return userService.get().uri("/users/{id}/phone", customerId).exchange(this::read);
        } catch (RestClientException failure) {
            throw new OwnerDirectoryUnavailable();
        }
    }

    private String read(
            HttpRequest request,
            RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse response) throws IOException {
        HttpStatusCode status = response.getStatusCode();
        if (status.value() == 404) {
            throw new CustomerNotFound();
        }
        if (status.value() == 401 || status.value() == 403) {
            throw new AccountLookupRejected();
        }
        if (!status.is2xxSuccessful()) {
            throw new OwnerDirectoryUnavailable();
        }
        return phone(response.bodyTo(PhonePayload.class));
    }

    private static String phone(PhonePayload body) {
        if (body == null || body.phone() == null || body.phone().isBlank()) {
            throw new OwnerDirectoryUnavailable();
        }
        return body.phone();
    }

    /** Body of {@code GET /users/{id}/phone}. The domain does not depend on this type. */
    record PhonePayload(String phone) {
    }
}

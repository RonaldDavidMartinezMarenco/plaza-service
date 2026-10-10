package com.foodcourt.plaza.infrastructure.output.client;

import java.io.IOException;
import java.util.Optional;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.foodcourt.plaza.domain.exception.AccountLookupRejected;
import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;

/**
 * Reads {@code GET /users/{id}} from user-service.
 * HTTP 404 is an empty account. Any other failure is {@link OwnerDirectoryUnavailable}.
 */
@Service
public final class UserServiceOwnerDirectory implements OwnerDirectory {

    private final RestClient userService;

    public UserServiceOwnerDirectory(RestClient userService) {
        this.userService = userService;
    }

    @Override
    public Optional<Account> findById(long id) {
        try {
            return userService.get().uri("/users/{id}", id).exchange(this::read);
        } catch (RestClientException failure) {
            throw new OwnerDirectoryUnavailable();
        }
    }

    private Optional<Account> read(
            HttpRequest request,
            RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse response) throws IOException {
        HttpStatusCode status = response.getStatusCode();
        if (isNotFound(status)) {
            return Optional.empty();
        }
        if (isRejected(status)) {
            throw new AccountLookupRejected();
        }
        if (!status.is2xxSuccessful()) {
            throw new OwnerDirectoryUnavailable();
        }
        return Optional.of(account(response.bodyTo(AccountPayload.class)));
    }

    private static boolean isNotFound(HttpStatusCode status) {
        return status.value() == 404;
    }

    private static boolean isRejected(HttpStatusCode status) {
        return status.value() == 401 || status.value() == 403;
    }

    private static Account account(AccountPayload body) {
        if (body == null || body.id() == null || body.role() == null) {
            throw new OwnerDirectoryUnavailable();
        }
        return new Account(body.id(), body.role());
    }

    /** Body of {@code GET /users/{id}}. The domain does not depend on this type. */
    record AccountPayload(Long id, Role role) {
    }
}

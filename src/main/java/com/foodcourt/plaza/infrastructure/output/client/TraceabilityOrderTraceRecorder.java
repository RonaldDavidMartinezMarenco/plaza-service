package com.foodcourt.plaza.infrastructure.output.client;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.foodcourt.plaza.domain.exception.TraceabilityRejected;
import com.foodcourt.plaza.domain.exception.TraceabilityUnavailable;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.OrderStatusChange;
import com.foodcourt.plaza.domain.spi.OrderTraceRecorder;

/**
 * Posts {@code /order-status-changes} to traceability-service.
 * HTTP 201 stores a new row. HTTP 200 is the same row sent again. HTTP 409 is a rejection.
 * Any other failure is unavailable.
 */
@Service
public final class TraceabilityOrderTraceRecorder implements OrderTraceRecorder {

    private final RestClient traceabilityService;

    public TraceabilityOrderTraceRecorder(@Qualifier("traceabilityServiceClient") RestClient traceabilityService) {
        this.traceabilityService = traceabilityService;
    }

    @Override
    public void record(OrderStatusChange change) {
        try {
            traceabilityService.post()
                    .uri("/order-status-changes")
                    .body(notice(change))
                    .exchange(this::read);
        } catch (RestClientException failure) {
            throw new TraceabilityUnavailable();
        }
    }

    private Void read(
            HttpRequest request,
            RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse response) throws IOException {
        int status = response.getStatusCode().value();
        if (accepted(status)) {
            return null;
        }
        if (status == 409) {
            throw new TraceabilityRejected();
        }
        throw new TraceabilityUnavailable();
    }

    private static boolean accepted(int status) {
        return status == 200 || status == 201;
    }

    private static Notice notice(OrderStatusChange change) {
        return new Notice(
                change.orderId(),
                change.customerId(),
                change.restaurantId(),
                change.employeeId(),
                change.previousStatus(),
                change.newStatus());
    }

    /** Body sent to traceability-service. It is not a domain type. */
    record Notice(
            long orderId,
            long customerId,
            long restaurantId,
            Long employeeId,
            OrderStatus previousStatus,
            OrderStatus newStatus) {
    }
}

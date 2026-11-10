package com.foodcourt.plaza.infrastructure.output.client;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.foodcourt.plaza.domain.exception.NotificationRejected;
import com.foodcourt.plaza.domain.exception.NotificationUnavailable;
import com.foodcourt.plaza.domain.spi.OrderReadyNotifier;

/**
 * Posts {@code /notifications/order-ready} to messaging-service.
 * HTTP 202 accepts the SMS. HTTP 502 is a rejection. Any other failure is unavailable.
 * The PIN is not written to a log.
 */
@Service
public final class MessagingOrderReadyNotifier implements OrderReadyNotifier {

    private final RestClient messagingService;

    public MessagingOrderReadyNotifier(@Qualifier("messagingServiceClient") RestClient messagingService) {
        this.messagingService = messagingService;
    }

    @Override
    public void notifyReady(long orderId, String restaurantName, String phone, String pin) {
        try {
            messagingService.post()
                    .uri("/notifications/order-ready")
                    .body(new Notice(orderId, restaurantName, phone, pin))
                    .exchange(this::read);
        } catch (RestClientException failure) {
            throw new NotificationUnavailable();
        }
    }

    private Void read(
            HttpRequest request,
            RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse response) throws IOException {
        HttpStatusCode status = response.getStatusCode();
        if (status.value() == 202) {
            return null;
        }
        if (status.value() == 502) {
            throw new NotificationRejected();
        }
        throw new NotificationUnavailable();
    }

    /** Body sent to messaging-service. It is not a domain type. */
    record Notice(long orderId, String restaurantName, String phone, String pin) {
    }
}

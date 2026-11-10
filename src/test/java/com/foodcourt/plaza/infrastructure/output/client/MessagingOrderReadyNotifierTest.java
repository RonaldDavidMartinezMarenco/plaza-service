package com.foodcourt.plaza.infrastructure.output.client;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.foodcourt.plaza.domain.exception.NotificationRejected;
import com.foodcourt.plaza.domain.exception.NotificationUnavailable;

class MessagingOrderReadyNotifierTest {

    private MockRestServiceServer server;
    private MessagingOrderReadyNotifier notifier;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://messaging-service");
        server = MockRestServiceServer.bindTo(builder).build();
        notifier = new MessagingOrderReadyNotifier(builder.build());
    }

    @Test
    void notifyReadyAcceptsAQueuedSms() {
        server.expect(requestTo("http://messaging-service/notifications/order-ready"))
                .andExpect(content().json("""
                        {"orderId":1,"restaurantName":"La Parrilla de Carlos","phone":"+573011590640","pin":"048215"}
                        """))
                .andRespond(withStatus(HttpStatus.ACCEPTED).contentType(MediaType.APPLICATION_JSON).body("{\"channel\":\"SMS\"}"));

        assertThatCode(() -> notifier.notifyReady(1L, "La Parrilla de Carlos", "+573011590640", "048215"))
                .doesNotThrowAnyException();
        server.verify();
    }

    @Test
    void notifyReadyRejectsAFailedSms() {
        server.expect(requestTo("http://messaging-service/notifications/order-ready"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> notifier.notifyReady(1L, "La Parrilla de Carlos", "+573011590640", "048215"))
                .isInstanceOf(NotificationRejected.class);
    }

    @Test
    void notifyReadyReportsAnyOtherStatusAsUnavailable() {
        server.expect(requestTo("http://messaging-service/notifications/order-ready"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> notifier.notifyReady(1L, "La Parrilla de Carlos", "+573011590640", "048215"))
                .isInstanceOf(NotificationUnavailable.class);
    }
}

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

import com.foodcourt.plaza.domain.exception.TraceabilityRejected;
import com.foodcourt.plaza.domain.exception.TraceabilityUnavailable;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.OrderStatusChange;

class TraceabilityOrderTraceRecorderTest {

    private MockRestServiceServer server;
    private TraceabilityOrderTraceRecorder recorder;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://traceability-service");
        server = MockRestServiceServer.bindTo(builder).build();
        recorder = new TraceabilityOrderTraceRecorder(builder.build());
    }

    @Test
    void recordAcceptsACreatedChange() {
        server.expect(requestTo("http://traceability-service/order-status-changes"))
                .andExpect(content().json("""
                        {
                          "orderId": 1,
                          "customerId": 9,
                          "restaurantId": 1,
                          "employeeId": null,
                          "previousStatus": null,
                          "newStatus": "PENDING"
                        }
                        """))
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body("{}"));

        assertThatCode(() -> recorder.record(opening())).doesNotThrowAnyException();
        server.verify();
    }

    @Test
    void recordAcceptsAnIdenticalReplay() {
        server.expect(requestTo("http://traceability-service/order-status-changes"))
                .andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("{}"));

        assertThatCode(() -> recorder.record(opening())).doesNotThrowAnyException();
    }

    @Test
    void recordRejectsAConflictingChange() {
        server.expect(requestTo("http://traceability-service/order-status-changes"))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        assertThatThrownBy(() -> recorder.record(opening())).isInstanceOf(TraceabilityRejected.class);
    }

    @Test
    void recordReportsAnyOtherStatusAsUnavailable() {
        server.expect(requestTo("http://traceability-service/order-status-changes"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> recorder.record(opening())).isInstanceOf(TraceabilityUnavailable.class);
    }

    @Test
    void recordReportsAClosedServiceAsUnavailable() {
        TraceabilityOrderTraceRecorder offline = new TraceabilityOrderTraceRecorder(
                RestClient.builder().baseUrl("http://127.0.0.1:1").build());

        assertThatThrownBy(() -> offline.record(opening())).isInstanceOf(TraceabilityUnavailable.class);
    }

    private static OrderStatusChange opening() {
        return new OrderStatusChange(1L, 9L, 1L, null, null, OrderStatus.PENDING);
    }
}

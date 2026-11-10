package com.foodcourt.plaza.infrastructure.output.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.foodcourt.plaza.domain.exception.AccountLookupRejected;
import com.foodcourt.plaza.domain.exception.CustomerNotFound;
import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;

class UserServiceCustomerPhoneDirectoryTest {

    private MockRestServiceServer server;
    private UserServiceCustomerPhoneDirectory directory;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://user-service");
        server = MockRestServiceServer.bindTo(builder).build();
        directory = new UserServiceCustomerPhoneDirectory(builder.build());
    }

    @Test
    void findPhoneReturnsTheCustomerPhone() {
        server.expect(requestTo("http://user-service/users/9/phone"))
                .andRespond(withSuccess("{\"phone\":\"+573011590640\"}", MediaType.APPLICATION_JSON));

        assertThat(directory.findPhone(9L)).isEqualTo("+573011590640");
        server.verify();
    }

    @Test
    void findPhoneIsMissingWhenTheAccountIsNotACustomer() {
        server.expect(requestTo("http://user-service/users/2/phone")).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> directory.findPhone(2L)).isInstanceOf(CustomerNotFound.class);
        server.verify();
    }

    @Test
    void findPhoneRejectsAnUnauthorizedDirectory() {
        server.expect(requestTo("http://user-service/users/9/phone")).andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> directory.findPhone(9L)).isInstanceOf(AccountLookupRejected.class);
    }

    @Test
    void findPhoneReportsAnUnavailableDirectory() {
        server.expect(requestTo("http://user-service/users/9/phone")).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> directory.findPhone(9L)).isInstanceOf(OwnerDirectoryUnavailable.class);
    }
}

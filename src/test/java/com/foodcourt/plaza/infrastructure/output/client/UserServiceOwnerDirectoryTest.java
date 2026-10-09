package com.foodcourt.plaza.infrastructure.output.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.Role;

class UserServiceOwnerDirectoryTest {

    private MockRestServiceServer server;
    private UserServiceOwnerDirectory directory;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://user-service");
        server = MockRestServiceServer.bindTo(builder).build();
        directory = new UserServiceOwnerDirectory(builder.build());
    }

    @Test
    void findByIdReturnsAnOwner() {
        server.expect(requestTo("http://user-service/users/15"))
                .andRespond(withSuccess("{\"id\":15,\"role\":\"OWNER\"}", MediaType.APPLICATION_JSON));

        Optional<Account> account = directory.findById(15L);

        assertThat(account).contains(new Account(15L, Role.OWNER));
        server.verify();
    }

    @Test
    void findByIdIsEmptyWhenTheAccountIsMissing() {
        server.expect(requestTo("http://user-service/users/15"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(directory.findById(15L)).isEmpty();
        server.verify();
    }

    @Test
    void findByIdRejectsAServerError() {
        server.expect(requestTo("http://user-service/users/15"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> directory.findById(15L)).isInstanceOf(OwnerDirectoryUnavailable.class);
        server.verify();
    }
}

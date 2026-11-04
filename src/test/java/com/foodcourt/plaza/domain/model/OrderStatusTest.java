package com.foodcourt.plaza.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.foodcourt.plaza.domain.exception.InvalidOrderStatus;

class OrderStatusTest {

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void parseExactAcceptsEachStoredName(OrderStatus status) {
        assertThat(OrderStatus.parseExact(status.name())).isEqualTo(status);
        assertThatCode(() -> OrderStatus.requireOne(new String[] {status.name()})).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", " pending", "pending", "PENDING ", "PENDIENTE", "READY,DELIVERED", "FOO"})
    void parseExactRejectsAnythingThatIsNotAStoredName(String raw) {
        assertThatThrownBy(() -> OrderStatus.parseExact(raw)).isInstanceOf(InvalidOrderStatus.class);
    }

    @Test
    void parseExactRejectsNull() {
        assertThatThrownBy(() -> OrderStatus.parseExact(null)).isInstanceOf(InvalidOrderStatus.class);
    }

    @ParameterizedTest
    @NullSource
    void requireOneRejectsAMissingValue(String[] raw) {
        assertThatThrownBy(() -> OrderStatus.requireOne(raw)).isInstanceOf(InvalidOrderStatus.class);
    }

    @Test
    void requireOneRejectsZeroOrSeveralValues() {
        assertThatThrownBy(() -> OrderStatus.requireOne(new String[0])).isInstanceOf(InvalidOrderStatus.class);
        assertThatThrownBy(() -> OrderStatus.requireOne(new String[] {"PENDING", "READY"}))
                .isInstanceOf(InvalidOrderStatus.class);
    }
}

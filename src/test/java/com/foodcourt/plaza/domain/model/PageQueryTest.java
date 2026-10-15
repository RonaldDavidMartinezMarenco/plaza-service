package com.foodcourt.plaza.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.foodcourt.plaza.domain.exception.InvalidPageRequest;

class PageQueryTest {

    @ParameterizedTest
    @CsvSource({"0,1", "0,50", "42949672,50"})
    void acceptsAPageInsideTheLimits(int page, int size) {
        PageQuery query = new PageQuery(page, size);

        assertThat(query.page()).isEqualTo(page);
        assertThat(query.size()).isEqualTo(size);
    }

    @ParameterizedTest
    @CsvSource({"-1,10", "0,0", "0,51", "2147483647,2"})
    void rejectsAPageOutsideTheLimits(int page, int size) {
        assertThatThrownBy(() -> new PageQuery(page, size))
                .isInstanceOf(InvalidPageRequest.class)
                .hasMessage(
                        "The page must be zero or more, the size between 1 and 50, and page times size must not exceed 2147483647");
    }
}

package com.foodcourt.plaza.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class PageResultTest {

    @Test
    void totalPagesIsZeroWhenThereAreNoItems() {
        PageResult<String> page = new PageResult<>(List.of(), 0, 10, 0);

        assertThat(page.totalPages()).isZero();
    }

    @Test
    void totalPagesRoundsUp() {
        assertThat(new PageResult<>(List.of("a"), 0, 10, 20).totalPages()).isEqualTo(2);
        assertThat(new PageResult<>(List.of("a"), 0, 10, 23).totalPages()).isEqualTo(3);
    }

    @Test
    void itemsStayUnchangedWhenTheSourceListChanges() {
        List<String> source = new ArrayList<>();
        source.add("a");
        PageResult<String> page = new PageResult<>(source, 0, 10, 1);

        source.add("b");

        assertThat(page.items()).containsExactly("a");
    }
}

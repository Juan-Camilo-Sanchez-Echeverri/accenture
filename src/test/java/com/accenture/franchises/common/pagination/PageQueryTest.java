package com.accenture.franchises.common.pagination;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PageQueryTest {

    @Test
    void keepsValidValues() {
        PageQuery query = PageQuery.of(2, 50);

        assertThat(query.page()).isEqualTo(2);
        assertThat(query.limit()).isEqualTo(50);
    }

    @Test
    void clampsPageToAtLeastZero() {
        assertThat(PageQuery.of(0, 20).page()).isZero();
        assertThat(PageQuery.of(-1, 20).page()).isZero();
    }

    @Test
    void clampsLimitToOneWhenZeroOrNegative() {
        assertThat(PageQuery.of(5, 0).limit()).isEqualTo(1);
        assertThat(PageQuery.of(5, -5).limit()).isEqualTo(1);
    }

    @Test
    void capsLimitAtTheMaximum() {
        assertThat(PageQuery.of(1, 1000).limit()).isEqualTo(PageQuery.MAX_LIMIT);
    }
}

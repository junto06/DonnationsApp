package com.donnations.backend.domain.model

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MoneyTest {

    @Test
    fun `parses major units into minor units`() {
        assertEquals(Money(2_550, Currency.EUR), Money.ofMajorOrNull(BigDecimal("25.5"), Currency.EUR))
        assertEquals(Money(1_000, Currency.EUR), Money.ofMajorOrNull(BigDecimal("10"), Currency.EUR))
    }

    @Test
    fun `rejects negative, over-precise and overflowing amounts`() {
        assertNull(Money.ofMajorOrNull(BigDecimal("-1.00"), Currency.EUR))
        assertNull(Money.ofMajorOrNull(BigDecimal("1.005"), Currency.EUR))
        assertNull(Money.ofMajorOrNull(BigDecimal("1e30"), Currency.EUR))
    }
}

package com.donnations.backend.domain.model

import com.donnations.backend.domain.campaign
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CampaignTest {

    @Test
    fun `rejects blank title`() {
        assertFailsWith<IllegalArgumentException> { campaign(title = " ") }
    }

    @Test
    fun `rejects zero goal`() {
        assertFailsWith<IllegalArgumentException> { campaign(goal = Money(0, Currency.USD)) }
    }

    @Test
    fun `rejects negative donor count`() {
        assertFailsWith<IllegalArgumentException> { campaign(donorCount = -1) }
    }

    @Test
    fun `rejects end before start`() {
        assertFailsWith<IllegalArgumentException> {
            campaign(createdAt = Instant.parse("2026-12-01T00:00:00Z"), endsAt = Instant.parse("2026-10-01T00:00:00Z"))
        }
    }

    @Test
    fun `money exposes major units using currency fraction digits`() {
        assertEquals(BigDecimal("32750.05"), Money(3_275_005, Currency.USD).amount)
    }

    @Test
    fun `rejects negative money`() {
        assertFailsWith<IllegalArgumentException> { Money(-1, Currency.USD) }
    }
}

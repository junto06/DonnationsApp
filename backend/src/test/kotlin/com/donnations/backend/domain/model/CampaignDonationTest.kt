package com.donnations.backend.domain.model

import com.donnations.backend.domain.campaign
import com.donnations.backend.domain.exception.CampaignEndedException
import com.donnations.backend.domain.exception.InvalidDonationAmountException
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CampaignDonationTest {

    private val open = Instant.parse("2026-11-01T00:00:00Z")
    private val campaign = campaign(raised = Money(2_500, Currency.EUR), donorCount = 3)

    @Test
    fun `receiving a donation adds to raised and counts the donor`() {
        val updated = campaign.receive(Money(1_000, Currency.EUR), open)

        assertEquals(Money(3_500, Currency.EUR), updated.raised)
        assertEquals(4, updated.donorCount)
    }

    @Test
    fun `rejects zero amount`() {
        assertFailsWith<InvalidDonationAmountException> { campaign.receive(Money(0, Currency.EUR), open) }
    }

    @Test
    fun `rejects amount that overflows the raised total`() {
        val nearlyFull = campaign(raised = Money(Long.MAX_VALUE - 10, Currency.EUR))
        assertFailsWith<InvalidDonationAmountException> { nearlyFull.receive(Money(11, Currency.EUR), open) }
    }

    @Test
    fun `rejects donation that overflows the donor count`() {
        val crowded = campaign(donorCount = Int.MAX_VALUE)
        assertFailsWith<InvalidDonationAmountException> { crowded.receive(Money(1_000, Currency.EUR), open) }
    }

    @Test
    fun `rejects donations at or after the end`() {
        assertFailsWith<CampaignEndedException> { campaign.receive(Money(1_000, Currency.EUR), campaign.endsAt) }
    }
}

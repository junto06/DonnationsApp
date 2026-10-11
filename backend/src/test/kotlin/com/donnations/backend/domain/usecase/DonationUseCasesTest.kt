package com.donnations.backend.domain.usecase

import com.donnations.backend.data.campaigns.CampaignsRepositoryImpl
import com.donnations.backend.data.campaigns.store.InMemoryCampaignsStore
import com.donnations.backend.domain.campaign
import com.donnations.backend.domain.exception.CampaignEndedException
import com.donnations.backend.domain.model.Currency
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DonationUseCasesTest {

    private val now = Instant.parse("2026-11-01T10:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val campaign = campaign(title = "Books")
    private val campaigns = CampaignsRepositoryImpl(InMemoryCampaignsStore(listOf(campaign)))
    private val getOptions = GetDonationOptionsUseCase(campaigns, clock)

    @Test
    fun `options carry campaign and suggested amounts in its currency`() {
        val options = getOptions(campaign.id)

        assertEquals("Books", options.campaign.title)
        assertEquals(listOf(1_000L, 2_500L, 5_000L, 10_000L), options.suggestedAmounts.map { it.amountMinor })
        assertEquals(setOf(Currency.EUR), options.suggestedAmounts.map { it.currency }.toSet())
    }

    @Test
    fun `options fail for ended campaign`() {
        val ended = GetDonationOptionsUseCase(campaigns, Clock.fixed(campaign.endsAt, ZoneOffset.UTC))
        assertFailsWith<CampaignEndedException> { ended(campaign.id) }
    }
}

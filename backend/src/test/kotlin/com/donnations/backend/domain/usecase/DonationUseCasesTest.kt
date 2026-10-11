package com.donnations.backend.domain.usecase

import com.donnations.backend.data.campaigns.CampaignsRepositoryImpl
import com.donnations.backend.data.campaigns.store.InMemoryCampaignsStore
import com.donnations.backend.data.donations.DonationsRepositoryImpl
import com.donnations.backend.data.donations.store.InMemoryDonationsStore
import com.donnations.backend.domain.campaign
import com.donnations.backend.domain.exception.CampaignEndedException
import com.donnations.backend.domain.exception.CampaignNotFoundException
import com.donnations.backend.domain.exception.InvalidDonationAmountException
import com.donnations.backend.domain.model.CampaignId
import com.donnations.backend.domain.model.Currency
import com.donnations.backend.domain.model.Money
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DonationUseCasesTest {

    private val now = Instant.parse("2026-11-01T10:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val campaign = campaign(title = "Books", raised = Money(2_500, Currency.EUR), donorCount = 3)
    private val campaigns = CampaignsRepositoryImpl(InMemoryCampaignsStore(listOf(campaign)))
    private val makeDonation = MakeDonationUseCase(campaigns, DonationsRepositoryImpl(InMemoryDonationsStore()), clock)
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

    @Test
    fun `donating records the donation and updates the campaign`() {
        val donation = makeDonation(campaign.id, BigDecimal("25.50"))

        assertEquals(Money(2_550, Currency.EUR), donation.amount)
        assertEquals(campaign.id, donation.campaignId)
        assertEquals(now, donation.createdAt)
        val updated = campaigns.getById(campaign.id)!!
        assertEquals(Money(5_050, Currency.EUR), updated.raised)
        assertEquals(4, updated.donorCount)
    }

    @Test
    fun `invalid amounts leave the campaign untouched`() {
        listOf("0", "-5", "1.001").forEach { amount ->
            assertFailsWith<InvalidDonationAmountException>(amount) { makeDonation(campaign.id, BigDecimal(amount)) }
        }
        assertEquals(campaign, campaigns.getById(campaign.id))
    }

    @Test
    fun `overflowing the raised total is an invalid amount and leaves the campaign untouched`() {
        val nearlyFull = campaign(raised = Money(Long.MAX_VALUE - 10, Currency.EUR))
        val repository = CampaignsRepositoryImpl(InMemoryCampaignsStore(listOf(nearlyFull)))
        val donate = MakeDonationUseCase(repository, DonationsRepositoryImpl(InMemoryDonationsStore()), clock)

        assertFailsWith<InvalidDonationAmountException> { donate(nearlyFull.id, BigDecimal("1.00")) }
        assertEquals(nearlyFull, repository.getById(nearlyFull.id))
    }

    @Test
    fun `donating to an ended campaign fails and leaves it untouched`() {
        val ended = MakeDonationUseCase(
            campaigns,
            DonationsRepositoryImpl(InMemoryDonationsStore()),
            Clock.fixed(campaign.endsAt, ZoneOffset.UTC),
        )

        assertFailsWith<CampaignEndedException> { ended(campaign.id, BigDecimal.TEN) }
        assertEquals(campaign, campaigns.getById(campaign.id))
    }

    @Test
    fun `donating to unknown campaign fails`() {
        assertFailsWith<CampaignNotFoundException> { makeDonation(CampaignId(UUID.randomUUID()), BigDecimal.TEN) }
    }
}

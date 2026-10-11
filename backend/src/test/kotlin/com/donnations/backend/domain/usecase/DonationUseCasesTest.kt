package com.donnations.backend.domain.usecase

import com.donnations.backend.data.campaigns.CampaignsRepositoryImpl
import com.donnations.backend.data.campaigns.store.InMemoryCampaignsStore
import com.donnations.backend.data.donations.DonationsRepositoryImpl
import com.donnations.backend.data.donations.store.InMemoryDonationsStore
import com.donnations.backend.domain.campaign
import com.donnations.backend.domain.exception.CampaignEndedException
import com.donnations.backend.domain.exception.CampaignNotFoundException
import com.donnations.backend.domain.exception.DonationConflictException
import com.donnations.backend.domain.exception.DonationNotFoundException
import com.donnations.backend.domain.exception.ErrorCode
import com.donnations.backend.domain.exception.InvalidDonationAmountException
import com.donnations.backend.domain.model.Campaign
import com.donnations.backend.domain.model.CampaignId
import com.donnations.backend.domain.model.Currency
import com.donnations.backend.domain.model.DonationId
import com.donnations.backend.domain.model.DonationProcessingPolicy
import com.donnations.backend.domain.model.DonationStatus
import com.donnations.backend.domain.model.Money
import com.donnations.backend.domain.queue.DonationQueue
import com.donnations.backend.domain.repository.CampaignsRepository
import java.math.BigDecimal
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.Collections
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DonationUseCasesTest {

    private val now = Instant.parse("2026-11-01T10:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val books = campaign(title = "Books", raised = Money(2_500, Currency.EUR), donorCount = 3)

    private val lease = Duration.ofSeconds(30)
    private val policy = DonationProcessingPolicy(lease)
    private val afterLease = Clock.fixed(now.plus(lease), ZoneOffset.UTC)

    private val queue = RecordingDonationQueue()
    private var campaigns: CampaignsRepository = CampaignsRepositoryImpl(InMemoryCampaignsStore(listOf(books)))
    private val donations = DonationsRepositoryImpl(InMemoryDonationsStore())

    private fun submitResult(
        id: DonationId = DonationId(UUID.randomUUID()),
        campaignId: CampaignId = books.id,
        amount: String = "25.50",
        at: Clock = clock,
    ) = SubmitDonationUseCase(campaigns, donations, queue, at)(id, campaignId, BigDecimal(amount))

    private fun submit(campaignId: CampaignId = books.id, amount: String = "25.50", at: Clock = clock) =
        submitResult(campaignId = campaignId, amount = amount, at = at).donation

    private fun process(id: DonationId, at: Clock = clock) = ProcessDonationUseCase(campaigns, donations, policy, at)(id)

    private fun recover(at: Clock) = RecoverStuckDonationsUseCase(donations, queue, policy, at)()

    private fun withCampaign(campaign: Campaign) {
        campaigns = CampaignsRepositoryImpl(InMemoryCampaignsStore(listOf(campaign)))
    }

    @Test
    fun `options carry campaign and suggested amounts in its currency`() {
        val options = GetDonationOptionsUseCase(campaigns, clock)(books.id)

        assertEquals("Books", options.campaign.title)
        assertTrue(options.acceptingDonations)
        assertEquals(listOf(1_000L, 2_500L, 5_000L, 10_000L), options.suggestedAmounts.map { it.amountMinor })
        assertEquals(setOf(Currency.EUR), options.suggestedAmounts.map { it.currency }.toSet())
    }

    @Test
    fun `ended campaign still has options but is not accepting donations`() {
        val options = GetDonationOptionsUseCase(campaigns, Clock.fixed(books.endsAt, ZoneOffset.UTC))(books.id)

        assertEquals("Books", options.campaign.title)
        assertFalse(options.acceptingDonations)
        assertEquals(emptyList(), options.suggestedAmounts)
    }

    @Test
    fun `submitting stores a pending donation and enqueues it without touching the campaign`() {
        val donation = submit()

        assertEquals(DonationStatus.PENDING, donation.status)
        assertEquals(Money(2_550, Currency.EUR), donation.amount)
        assertEquals(listOf(donation.id), queue.enqueued)
        assertEquals(donation, donations.getById(donation.id))
        assertEquals(books, campaigns.getById(books.id))
    }

    @Test
    fun `submitting rejects invalid amounts, unknown and ended campaigns synchronously`() {
        listOf("0", "-5", "1.001").forEach { amount ->
            assertFailsWith<InvalidDonationAmountException>(amount) { submit(amount = amount) }
        }
        assertFailsWith<CampaignNotFoundException> { submit(campaignId = CampaignId(UUID.randomUUID())) }
        assertFailsWith<CampaignEndedException> { submit(at = Clock.fixed(books.endsAt, ZoneOffset.UTC)) }
        assertEquals(emptyList(), queue.enqueued)
    }

    @Test
    fun `retrying with the same id returns the original donation without enqueueing again`() {
        val id = DonationId(UUID.randomUUID())
        val first = submitResult(id = id, amount = "25.50")

        val retry = submitResult(id = id, amount = "25.5")

        assertTrue(first.created)
        assertFalse(retry.created)
        assertEquals(first.donation, retry.donation)
        assertEquals(listOf(id), queue.enqueued)
    }

    @Test
    fun `retry after processing returns the current status even once the campaign has ended`() {
        val id = DonationId(UUID.randomUUID())
        submitResult(id = id)
        process(id)

        val retry = submitResult(id = id, at = Clock.fixed(books.endsAt, ZoneOffset.UTC))

        assertEquals(DonationStatus.COMPLETED, retry.donation.status)
        assertEquals(4, campaigns.getById(books.id)!!.donorCount)
    }

    @Test
    fun `reusing an id for a different amount or campaign is a conflict`() {
        val id = DonationId(UUID.randomUUID())
        submitResult(id = id, amount = "25.50")

        assertFailsWith<DonationConflictException> { submitResult(id = id, amount = "30.00") }
        assertFailsWith<DonationConflictException> { submitResult(id = id, campaignId = CampaignId(UUID.randomUUID())) }
        assertEquals(listOf(id), queue.enqueued)
    }

    @Test
    fun `concurrent submits with the same id create one donation`() {
        val id = DonationId(UUID.randomUUID())
        val threads = 16
        val start = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(threads)
        val created = AtomicInteger()

        repeat(threads) { pool.execute { start.await(); if (submitResult(id = id).created) created.incrementAndGet() } }
        start.countDown()
        pool.shutdown()
        pool.awaitTermination(5, TimeUnit.SECONDS)

        assertEquals(1, created.get())
        assertEquals(listOf(id), queue.enqueued.toList())
    }

    @Test
    fun `processing completes the donation and updates the campaign`() {
        val id = submit().id

        process(id)

        val donation = donations.getById(id)!!
        assertEquals(DonationStatus.COMPLETED, donation.status)
        assertEquals(now, donation.processedAt)
        assertNull(donation.failure)
        val updated = campaigns.getById(books.id)!!
        assertEquals(Money(5_050, Currency.EUR), updated.raised)
        assertEquals(4, updated.donorCount)
    }

    @Test
    fun `processing twice counts the donation once`() {
        val id = submit().id

        process(id)
        process(id)

        assertEquals(4, campaigns.getById(books.id)!!.donorCount)
    }

    @Test
    fun `claiming is exclusive`() {
        val id = submit().id

        assertEquals(DonationStatus.PROCESSING, donations.claim(id, now, lease)?.status)
        assertNull(donations.claim(id, now.plus(lease).minusMillis(1), lease))
        assertNull(donations.claim(DonationId(UUID.randomUUID()), now, lease))
    }

    @Test
    fun `concurrent deliveries of the same donation apply it once`() {
        val id = submit().id
        val workers = 16
        val start = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(workers)

        repeat(workers) { pool.execute { start.await(); process(id) } }
        start.countDown()
        pool.shutdown()
        pool.awaitTermination(5, TimeUnit.SECONDS)

        assertEquals(DonationStatus.COMPLETED, donations.getById(id)!!.status)
        assertEquals(4, campaigns.getById(books.id)!!.donorCount)
        assertEquals(Money(5_050, Currency.EUR), campaigns.getById(books.id)!!.raised)
    }

    @Test
    fun `a donation whose worker died before the campaign update is retried after the lease`() {
        val id = submit().id
        donations.claim(id, now, lease) // a worker claimed it and crashed

        process(id)
        assertEquals(DonationStatus.PROCESSING, donations.getById(id)!!.status)

        process(id, at = afterLease)
        assertEquals(DonationStatus.COMPLETED, donations.getById(id)!!.status)
        assertEquals(4, campaigns.getById(books.id)!!.donorCount)
    }

    @Test
    fun `a donation whose worker died after the campaign update completes without counting twice`() {
        val id = submit().id
        val claimed = donations.claim(id, now, lease)!!
        campaigns.applyDonation(books.id, id) { it.receive(claimed.amount, now) } // crashed before saving COMPLETED

        process(id, at = afterLease)

        assertEquals(DonationStatus.COMPLETED, donations.getById(id)!!.status)
        assertEquals(4, campaigns.getById(books.id)!!.donorCount)
        assertEquals(Money(5_050, Currency.EUR), campaigns.getById(books.id)!!.raised)
    }

    @Test
    fun `an unexpected failure leaves the claim to expire and recovery finishes it`() {
        val flaky = FlakyCampaignsRepository(campaigns)
        campaigns = flaky
        val id = submit().id

        assertFailsWith<IllegalStateException> { process(id) }
        assertEquals(DonationStatus.PROCESSING, donations.getById(id)!!.status)

        queue.enqueued.clear()
        assertEquals(1, recover(afterLease))
        assertEquals(listOf(id), queue.enqueued)
        process(id, at = afterLease)
        assertEquals(DonationStatus.COMPLETED, donations.getById(id)!!.status)
        assertEquals(4, campaigns.getById(books.id)!!.donorCount)
    }

    @Test
    fun `recovery re-enqueues only donations idle longer than the lease`() {
        val oldPending = submit().id
        val expiredClaim = submit().id.also { donations.claim(it, now, lease) }
        val completed = submit().id.also { process(it) }
        val freshPending = submit(at = Clock.fixed(now.plusSeconds(10), ZoneOffset.UTC)).id
        queue.enqueued.clear()

        recover(afterLease)

        assertEquals(setOf(oldPending, expiredClaim), queue.enqueued.toSet())
        assertFalse(completed in queue.enqueued || freshPending in queue.enqueued)
    }

    @Test
    fun `applying the same donation to a campaign twice counts it once`() {
        val donationId = DonationId(UUID.randomUUID())
        val add = { campaign: Campaign -> campaign.receive(Money(1_000, Currency.EUR), now) }

        campaigns.applyDonation(books.id, donationId, add)
        campaigns.applyDonation(books.id, donationId, add)

        assertEquals(4, campaigns.getById(books.id)!!.donorCount)
    }

    @Test
    fun `processing fails the donation when the campaign ended after submission`() {
        val id = submit().id

        process(id, at = Clock.fixed(books.endsAt, ZoneOffset.UTC))

        assertEquals(DonationStatus.FAILED, donations.getById(id)!!.status)
        assertEquals(ErrorCode.CAMPAIGN_ENDED, donations.getById(id)!!.failure)
        assertEquals(books, campaigns.getById(books.id))
    }

    @Test
    fun `processing fails the donation when it would overflow the raised total`() {
        val nearlyFull = books.copy(raised = Money(Long.MAX_VALUE - 10, Currency.EUR))
        withCampaign(nearlyFull)
        val id = submit(amount = "1.00").id

        process(id)

        assertEquals(ErrorCode.INVALID_DONATION_AMOUNT, donations.getById(id)!!.failure)
        assertEquals(nearlyFull, campaigns.getById(books.id))
    }

    @Test
    fun `getting an unknown donation fails`() {
        assertFailsWith<DonationNotFoundException> { GetDonationUseCase(donations)(DonationId(UUID.randomUUID())) }
    }

    // Throws once on the first donation applied, like a dropped database connection.
    private class FlakyCampaignsRepository(private val delegate: CampaignsRepository) : CampaignsRepository by delegate {
        private var failed = false

        override fun applyDonation(id: CampaignId, donationId: DonationId, transform: (Campaign) -> Campaign): Campaign? {
            if (!failed) {
                failed = true
                throw IllegalStateException("connection lost")
            }
            return delegate.applyDonation(id, donationId, transform)
        }
    }

    private class RecordingDonationQueue : DonationQueue {
        val enqueued: MutableList<DonationId> = Collections.synchronizedList(mutableListOf())

        override fun enqueue(id: DonationId) {
            enqueued += id
        }
    }
}

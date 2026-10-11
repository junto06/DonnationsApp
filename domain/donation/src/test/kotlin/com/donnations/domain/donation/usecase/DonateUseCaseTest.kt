package com.donnations.domain.donation.usecase

import com.donnations.core.model.CampaignId
import com.donnations.core.model.DonationId
import com.donnations.domain.donation.exception.CampaignEndedException
import com.donnations.domain.donation.exception.DonationStillProcessingException
import com.donnations.domain.donation.model.Donation
import com.donnations.domain.donation.model.DonationOptions
import com.donnations.domain.donation.model.DonationStatus
import com.donnations.domain.donation.model.DonationStatus.COMPLETED
import com.donnations.domain.donation.model.DonationStatus.IN_PROGRESS
import com.donnations.domain.donation.repository.DonationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

class DonateUseCaseTest {

    private val id = DonationId("d1")
    private val campaignId = CampaignId("c1")

    private fun TestScope.repository(submit: DonationStatus, vararg polls: DonationStatus) =
        FakeDonationRepository(submit, polls.toMutableList()) { testScheduler.currentTime }

    private suspend fun DonateUseCase.donate() = this(id, campaignId, BigDecimal("25"))

    @Test
    fun `a donation completed on submit returns without polling`() = runTest {
        val repository = repository(submit = COMPLETED)

        val donation = DonateUseCase(repository).donate()

        assertEquals(COMPLETED, donation.status)
        assertEquals(emptyList<Long>(), repository.pollTimes)
        assertEquals(listOf(BigDecimal("25")), repository.submittedAmounts)
    }

    @Test
    fun `polls with the submitted id until the donation completes`() = runTest {
        val repository = repository(IN_PROGRESS, IN_PROGRESS, COMPLETED)

        val donation = DonateUseCase(repository).donate()

        assertEquals(COMPLETED, donation.status)
        assertEquals(listOf(id, id), repository.polledIds)
    }

    @Test
    fun `waits grow from 300 ms by 1_5x up to a 2 s cap`() = runTest {
        val repository = repository(IN_PROGRESS, IN_PROGRESS)

        runCatching { DonateUseCase(repository).donate() }

        val gaps = (listOf(0L) + repository.pollTimes).zipWithNext { a, b -> b - a }
        assertEquals(listOf(300L, 450L, 675L), gaps.take(3))
        assertTrue(gaps.dropLast(1).zipWithNext().all { (a, b) -> b >= a })
        assertTrue(gaps.all { it <= 2_000L })
    }

    @Test
    fun `gives up with still processing once the 10 s budget is spent, never waiting longer`() = runTest {
        val repository = repository(IN_PROGRESS, IN_PROGRESS)

        val failure = runCatching { DonateUseCase(repository).donate() }.exceptionOrNull()

        assertTrue(failure is DonationStillProcessingException)
        // The last pause is clamped, so the total wait lands on the budget (plus ms rounding), not past it.
        assertTrue(testScheduler.currentTime in 10_000L..10_010L)
    }

    @Test
    fun `a failure reported while polling propagates`() = runTest {
        val repository = repository(IN_PROGRESS).apply { pollFailure = CampaignEndedException() }

        assertTrue(runCatching { DonateUseCase(repository).donate() }.exceptionOrNull() is CampaignEndedException)
    }

    // Each poll returns the next status; the last one repeats.
    private class FakeDonationRepository(
        private val submitStatus: DonationStatus,
        private val pollStatuses: MutableList<DonationStatus>,
        private val clock: () -> Long,
    ) : DonationRepository {
        var pollFailure: Throwable? = null
        val submittedAmounts = mutableListOf<BigDecimal>()
        val polledIds = mutableListOf<DonationId>()
        val pollTimes = mutableListOf<Long>()

        override fun getDonationOptions(campaignId: CampaignId): Flow<DonationOptions> = emptyFlow()

        override suspend fun submitDonation(id: DonationId, campaignId: CampaignId, amount: BigDecimal): Donation {
            submittedAmounts += amount
            return donation(id, submitStatus)
        }

        override suspend fun getDonation(id: DonationId): Donation {
            polledIds += id
            pollTimes += clock()
            pollFailure?.let { throw it }
            val status = if (pollStatuses.size > 1) pollStatuses.removeAt(0) else pollStatuses.first()
            return donation(id, status)
        }

        private fun donation(id: DonationId, status: DonationStatus) = Donation(
            id = id,
            campaignId = CampaignId("c1"),
            amount = BigDecimal("25"),
            currencyCode = "EUR",
            createdAt = Instant.parse("2026-10-11T10:00:00Z"),
            status = status,
        )
    }
}

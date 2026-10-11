package com.donnations.feature.donation

import app.cash.turbine.test
import com.donnations.core.model.CampaignId
import com.donnations.core.model.DonationId
import com.donnations.domain.donation.exception.CampaignEndedException
import com.donnations.domain.donation.exception.CampaignNotFoundException
import com.donnations.domain.donation.exception.InvalidDonationAmountException
import com.donnations.domain.donation.model.Donation
import com.donnations.domain.donation.model.DonationOptions
import com.donnations.domain.donation.model.DonationStatus
import com.donnations.domain.donation.repository.DonationRepository
import com.donnations.domain.donation.usecase.DonateUseCase
import com.donnations.feature.donation.model.DonationUiMapper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class DonationViewModelTest {

    private val repository = FakeDonationRepository()

    private fun viewModel(locale: Locale = Locale.US) = DonationViewModel(
        campaignId = "c1",
        repository = repository,
        donateUseCase = DonateUseCase(repository),
        mapper = DonationUiMapper({ locale }, testStrings),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads options for the campaign from the navigation argument`() = runTest {
        viewModel().uiState.test {
            val state = awaitItem() as DonationUiState.Success
            assertEquals("Books for Every Child", state.donation.campaignTitle)
            assertEquals(listOf("c1"), repository.requestedIds)
        }
    }

    @Test
    fun `tapping a preset fills the amount and editing it drops the highlight, without reloading`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            val loaded = awaitItem() as DonationUiState.Success

            viewModel.changeAmount(loaded.donation.presets[2].inputValue)
            val preset = awaitItem() as DonationUiState.Success
            assertEquals("50", preset.donation.amount)
            assertTrue(preset.donation.presets[2].selected)

            viewModel.changeAmount("0")
            val edited = awaitItem() as DonationUiState.Success
            assertTrue(edited.donation.presets.none { it.selected })
            assertEquals("Enter a valid amount", edited.donation.amountError)
            assertEquals(1, repository.requestedIds.size)
        }
    }

    @Test
    fun `donating sends the parsed amount and shows the thank-you message`() = runTest {
        val viewModel = viewModel(Locale.GERMANY)
        viewModel.uiState.test {
            awaitItem()
            viewModel.changeAmount("12,5")
            awaitItem()

            viewModel.donate()

            val donated = expectMostRecentItem() as DonationUiState.Donated
            assertEquals("Thank you! Your 12,50\u00A0€ donation to Books for Every Child has been received.", donated.message)
            assertEquals(listOf(BigDecimal("12.5")), repository.donatedAmounts)
        }
    }

    @Test
    fun `donate does nothing while the amount is empty or invalid`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitItem()
            viewModel.donate()
            viewModel.changeAmount("1,000")
            awaitItem()
            viewModel.donate()

            assertEquals(emptyList<BigDecimal>(), repository.donatedAmounts)
        }
    }

    @Test
    fun `a second tap while submitting sends only one donation`() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitItem()
            viewModel.changeAmount("25")
            awaitItem()

            viewModel.donate()
            val submitting = awaitItem() as DonationUiState.Success
            assertTrue(submitting.donation.isSubmitting)
            viewModel.donate()

            gate.complete(Unit)
            assertTrue(awaitItem() is DonationUiState.Donated)
            assertEquals(1, repository.donatedAmounts.size)
        }
    }

    @Test
    fun `a failed donation shows an error that editing the amount clears`() = runTest {
        repository.submitFailure = InvalidDonationAmountException()
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitItem()
            viewModel.changeAmount("25")
            awaitItem()

            viewModel.donate()
            val failed = expectMostRecentItem() as DonationUiState.Success
            assertEquals("This amount can't be accepted.", failed.donation.submitError)

            viewModel.changeAmount("30")
            val edited = expectMostRecentItem() as DonationUiState.Success
            assertEquals("30", edited.donation.amount)
            assertNull(edited.donation.submitError)
        }
    }

    @Test
    fun `network failures on submit show a generic message`() = runTest {
        repository.submitFailure = IllegalStateException("offline")
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitItem()
            viewModel.changeAmount("25")
            awaitItem()
            viewModel.donate()

            assertEquals("Couldn't confirm your donation.", (expectMostRecentItem() as DonationUiState.Success).donation.submitError)
        }
    }

    @Test
    fun `an ended campaign shows its title instead of the form`() = runTest {
        repository.acceptingDonations = false

        viewModel().uiState.test {
            assertEquals(DonationUiState.Ended("Books for Every Child"), awaitItem())
        }
    }

    @Test
    fun `retrying the same amount reuses the donation id, a new amount gets a new one`() = runTest {
        repository.submitFailure = IllegalStateException("response lost")
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitItem()
            viewModel.changeAmount("25")
            awaitItem()
            viewModel.donate()
            expectMostRecentItem()

            viewModel.changeAmount("25.00")
            expectMostRecentItem()
            viewModel.donate()
            expectMostRecentItem()

            viewModel.changeAmount("30")
            expectMostRecentItem()
            viewModel.donate()
            expectMostRecentItem()
        }

        val (first, retry, changed) = repository.donationIds
        assertEquals(first, retry)
        assertTrue(first != changed)
    }

    @Test
    fun `two successful donations of the same amount use different ids`() = runTest {
        val first = viewModel()
        first.uiState.test {
            awaitItem()
            first.changeAmount("25")
            awaitItem()
            first.donate()
            expectMostRecentItem()
        }
        val second = viewModel()
        second.uiState.test {
            awaitItem()
            second.changeAmount("25")
            awaitItem()
            second.donate()
            expectMostRecentItem()
        }

        assertEquals(2, repository.donationIds.toSet().size)
    }

    @Test
    fun `a donation still processing after the poll budget shows a message that invites a safe retry`() = runTest {
        repository.submitStatus = DonationStatus.IN_PROGRESS
        repository.pollStatuses = mutableListOf(DonationStatus.IN_PROGRESS) // never settles
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitItem()
            viewModel.changeAmount("25")
            awaitItem()
            viewModel.donate()
            testScheduler.advanceUntilIdle()

            assertEquals(
                "Your donation is still being processed.",
                (expectMostRecentItem() as DonationUiState.Success).donation.submitError,
            )
        }
    }

    @Test
    fun `a failure while polling shows its message`() = runTest {
        repository.submitStatus = DonationStatus.IN_PROGRESS
        repository.pollFailure = CampaignEndedException()
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitItem()
            viewModel.changeAmount("25")
            awaitItem()
            viewModel.donate()
            testScheduler.advanceUntilIdle()

            assertEquals(
                "This campaign has ended.",
                (expectMostRecentItem() as DonationUiState.Success).donation.submitError,
            )
        }
    }

    @Test
    fun `ended and unknown campaigns are final load errors, others can be retried`() = runTest {
        mapOf(
            CampaignEndedException() to DonationUiState.Error(R.string.donation_error_campaign_ended, retryable = false),
            CampaignNotFoundException() to DonationUiState.Error(R.string.donation_error_campaign_not_found, retryable = false),
            IllegalStateException("offline") to DonationUiState.Error(R.string.donation_error_load, retryable = true),
        ).forEach { (failure, expected) ->
            repository.loadFailure = failure
            viewModel().uiState.test { assertEquals(expected, awaitItem()) }
        }
    }

    @Test
    fun `retry recovers after a failure`() = runTest {
        repository.loadFailure = IllegalStateException("offline")
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is DonationUiState.Error)

            repository.loadFailure = null
            viewModel.retry()

            // StateFlow may conflate the intermediate Loading.
            val next = awaitItem().let { if (it == DonationUiState.Loading) awaitItem() else it }
            assertTrue(next is DonationUiState.Success)
        }
    }

    private class FakeDonationRepository : DonationRepository {
        var loadFailure: Throwable? = null
        var submitFailure: Throwable? = null
        var pollFailure: Throwable? = null
        var gate: CompletableDeferred<Unit>? = null
        var acceptingDonations = true
        var submitStatus = DonationStatus.COMPLETED
        var pollStatuses = mutableListOf(DonationStatus.COMPLETED)
        val requestedIds = mutableListOf<String>()
        val donatedAmounts = mutableListOf<BigDecimal>()
        val donationIds = mutableListOf<DonationId>()
        val polledIds = mutableListOf<DonationId>()

        override fun getDonationOptions(campaignId: CampaignId): Flow<DonationOptions> = flow {
            requestedIds += campaignId.value
            loadFailure?.let { throw it }
            emit(donationOptions(campaignId.value, acceptingDonations))
        }

        override suspend fun submitDonation(id: DonationId, campaignId: CampaignId, amount: BigDecimal): Donation {
            donatedAmounts += amount
            donationIds += id
            gate?.await()
            submitFailure?.let { throw it }
            return donation(amount = amount.toPlainString(), campaignId = campaignId.value, status = submitStatus)
        }

        override suspend fun getDonation(id: DonationId): Donation {
            polledIds += id
            pollFailure?.let { throw it }
            val status = if (pollStatuses.size > 1) pollStatuses.removeAt(0) else pollStatuses.first()
            return donation(status = status)
        }
    }
}

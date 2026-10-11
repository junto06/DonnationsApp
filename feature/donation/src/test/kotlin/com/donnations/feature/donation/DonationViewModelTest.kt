package com.donnations.feature.donation

import app.cash.turbine.test
import com.donnations.core.model.CampaignId
import com.donnations.domain.donation.exception.CampaignEndedException
import com.donnations.domain.donation.exception.CampaignNotFoundException
import com.donnations.domain.donation.model.DonationOptions
import com.donnations.domain.donation.repository.DonationRepository
import com.donnations.feature.donation.model.DonationUiMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

class DonationViewModelTest {

    private val repository = FakeDonationRepository()
    private val viewModel by lazy {
        DonationViewModel(
            campaignId = "c1",
            repository = repository,
            mapper = DonationUiMapper({ Locale.US }, testStrings),
        )
    }

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
        viewModel.uiState.test {
            val state = awaitItem() as DonationUiState.Success
            assertEquals("Books for Every Child", state.donation.campaignTitle)
            assertEquals(listOf("c1"), repository.requestedIds)
        }
    }

    @Test
    fun `tapping a preset fills the amount and editing it drops the highlight, without reloading`() = runTest {
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
    fun `ended and unknown campaigns are final errors, others can be retried`() = runTest {
        mapOf(
            CampaignEndedException() to DonationUiState.Error(R.string.donation_error_campaign_ended, retryable = false),
            CampaignNotFoundException() to DonationUiState.Error(R.string.donation_error_campaign_not_found, retryable = false),
            IllegalStateException("offline") to DonationUiState.Error(R.string.donation_error_load, retryable = true),
        ).forEach { (failure, expected) ->
            repository.failure = failure
            DonationViewModel("c1", repository, DonationUiMapper({ Locale.US }, testStrings)).uiState.test {
                assertEquals(expected, awaitItem())
            }
        }
    }

    @Test
    fun `retry recovers after a failure`() = runTest {
        repository.failure = IllegalStateException("offline")

        viewModel.uiState.test {
            assertTrue(awaitItem() is DonationUiState.Error)

            repository.failure = null
            viewModel.retry()

            // StateFlow may conflate the intermediate Loading.
            val next = awaitItem().let { if (it == DonationUiState.Loading) awaitItem() else it }
            assertTrue(next is DonationUiState.Success)
        }
    }

    private class FakeDonationRepository : DonationRepository {
        var failure: Throwable? = null
        val requestedIds = mutableListOf<String>()

        override fun getDonationOptions(campaignId: CampaignId): Flow<DonationOptions> = flow {
            requestedIds += campaignId.value
            failure?.let { throw it }
            emit(donationOptions(campaignId.value))
        }
    }
}

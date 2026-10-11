package com.donnations.feature.campaign

import app.cash.turbine.test
import com.donnations.core.model.CampaignId
import com.donnations.domain.campaign.model.CampaignDetail
import com.donnations.domain.campaign.repository.CampaignRepository
import com.donnations.feature.campaign.model.CampaignDetailUiMapper
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

class CampaignDetailViewModelTest {

    private val repository = FakeCampaignRepository()
    private val viewModel by lazy {
        CampaignDetailViewModel(
            campaignId = "c1",
            repository = repository,
            mapper = CampaignDetailUiMapper({ Locale.US }, testStrings),
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
    fun `loads the campaign from the navigation argument`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is CampaignDetailUiState.Success)
            assertEquals("Medical Support", (state as CampaignDetailUiState.Success).campaign.title)
            assertEquals(listOf("c1"), repository.requestedIds)
        }
    }

    @Test
    fun `emits error when loading fails and recovers on retry`() = runTest {
        repository.failure = IllegalStateException("offline")

        viewModel.uiState.test {
            assertEquals(CampaignDetailUiState.Error(R.string.campaign_detail_error_load), awaitItem())

            repository.failure = null
            viewModel.retry()

            // StateFlow may conflate the intermediate Loading.
            val next = awaitItem().let { if (it == CampaignDetailUiState.Loading) awaitItem() else it }
            assertTrue(next is CampaignDetailUiState.Success)
        }
    }

    private class FakeCampaignRepository : CampaignRepository {
        var failure: Throwable? = null
        val requestedIds = mutableListOf<String>()

        override fun getCampaign(id: CampaignId): Flow<CampaignDetail> = flow {
            requestedIds += id.value
            failure?.let { throw it }
            emit(campaignDetail(id = id.value))
        }
    }
}

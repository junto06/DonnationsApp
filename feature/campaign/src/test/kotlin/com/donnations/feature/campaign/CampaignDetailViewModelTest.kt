package com.donnations.feature.campaign

import app.cash.turbine.test
import com.donnations.core.base.testExceptionHandler
import com.donnations.core.model.Cached
import com.donnations.core.model.Data
import com.donnations.core.model.CampaignId
import com.donnations.domain.campaign.model.CampaignDetail
import com.donnations.domain.campaign.repository.CampaignRepository
import com.donnations.feature.campaign.model.CampaignDetailUiMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.Instant
import java.util.Locale

class CampaignDetailViewModelTest {

    private val repository = FakeCampaignRepository()
    private val viewModel by lazy {
        CampaignDetailViewModel(
            campaignId = "c1",
            repository = repository,
            mapper = CampaignDetailUiMapper({ Locale.US }, testStrings),
            exceptionHandler = testExceptionHandler,
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
        repository.stored.value = Cached(campaignDetail(), Instant.EPOCH)

        viewModel.uiState.test {
            val state = awaitItem() as CampaignDetailUiState.Success
            assertEquals("Medical Support", state.campaign.title)
            assertEquals(listOf("c1"), repository.requestedIds)
        }
    }

    @Test
    fun `keeps showing the stored campaign when the refresh fails`() = runTest {
        repository.stored.value = Cached(campaignDetail(), Instant.EPOCH)
        repository.error.value = IOException("offline")

        viewModel.uiState.test {
            assertTrue(awaitItem() is CampaignDetailUiState.Success)
        }
    }

    @Test
    fun `shows loading until the first refresh with nothing stored`() = runTest {
        viewModel.uiState.test {
            assertEquals(CampaignDetailUiState.Loading, awaitItem())

            repository.stored.value = Cached(campaignDetail(), Instant.EPOCH)

            assertTrue(awaitItem() is CampaignDetailUiState.Success)
        }
    }

    @Test
    fun `emits error when the refresh fails with nothing stored and retries by loading again`() = runTest {
        repository.error.value = IOException("offline")

        viewModel.uiState.test {
            assertEquals(CampaignDetailUiState.Error(R.string.campaign_detail_error_load), awaitItem())

            repository.error.value = null
            repository.stored.value = Cached(campaignDetail(), Instant.EPOCH)
            viewModel.retry()

            assertTrue(expectMostRecentItem() is CampaignDetailUiState.Success)
            assertEquals(listOf("c1", "c1"), repository.requestedIds)
        }
    }

    // The offline-first logic itself is tested in core:base; this only feeds its result.
    private class FakeCampaignRepository : CampaignRepository {
        val stored = MutableStateFlow<Cached<CampaignDetail>?>(null)
        val error = MutableStateFlow<Throwable?>(null)
        val requestedIds = mutableListOf<String>()

        override fun getCampaign(id: CampaignId): Flow<Data<Cached<CampaignDetail>>> =
            combine(stored, error) { stored, error -> Data(content = stored, error = error, loading = stored == null && error == null) }.onStart { requestedIds += id.value }
    }
}

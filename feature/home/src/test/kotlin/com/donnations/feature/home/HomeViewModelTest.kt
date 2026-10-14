package com.donnations.feature.home

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.donnations.core.base.testExceptionHandler
import com.donnations.core.model.Cached
import com.donnations.core.model.Data
import com.donnations.domain.home.model.Home
import com.donnations.domain.home.repository.HomeRepository
import com.donnations.feature.home.model.HomeUiMapper
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

class HomeViewModelTest {

    private val repository = FakeHomeRepository()
    private val viewModel by lazy { HomeViewModel(repository, HomeUiMapper({ Locale.US }, testStrings), SavedStateHandle(), testExceptionHandler) }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `shows the stored home while refreshing`() = runTest {
        repository.stored.value = Cached(home(), Instant.EPOCH)

        viewModel.uiState.test {
            val state = awaitItem() as HomeUiState.Success
            assertEquals(listOf("f1"), state.home.featured.map { it.id })
        }
    }

    @Test
    fun `shows loading until the first refresh stores home`() = runTest {
        viewModel.uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())

            repository.stored.value = Cached(home(), Instant.EPOCH)

            assertTrue(awaitItem() is HomeUiState.Success)
        }
    }

    @Test
    fun `keeps showing the stored home when the refresh fails`() = runTest {
        repository.stored.value = Cached(home(), Instant.EPOCH)
        repository.error.value = IOException("offline")

        viewModel.uiState.test {
            assertTrue(awaitItem() is HomeUiState.Success)
        }
    }

    @Test
    fun `emits error when the refresh fails with nothing stored and retries by loading again`() = runTest {
        repository.error.value = IOException("offline")

        viewModel.uiState.test {
            assertEquals(HomeUiState.Error(R.string.home_error_load), awaitItem())

            repository.error.value = null
            repository.stored.value = Cached(home(), Instant.EPOCH)
            viewModel.retry()

            assertTrue(expectMostRecentItem() is HomeUiState.Success)
            assertEquals(2, repository.loads)
        }
    }

    @Test
    fun `selecting a category filters locally without loading again`() = runTest {
        repository.stored.value = Cached(home(), Instant.EPOCH)

        viewModel.uiState.test {
            assertTrue(awaitItem() is HomeUiState.Success)

            viewModel.selectCategory("education")

            val state = awaitItem() as HomeUiState.Success
            assertEquals("education", state.home.selectedCategoryId)
            assertEquals(listOf("c2"), state.home.campaigns.map { it.id })
            assertEquals(1, repository.loads)
        }
    }

    // The offline-first logic itself is tested in core:base; this only feeds its result.
    private class FakeHomeRepository : HomeRepository {
        val stored = MutableStateFlow<Cached<Home>?>(null)
        val error = MutableStateFlow<Throwable?>(null)
        var loads = 0

        override fun getHome(): Flow<Data<Cached<Home>>> =
            combine(stored, error) { stored, error -> Data(content = stored, error = error, loading = stored == null && error == null) }.onStart { loads++ }
    }
}

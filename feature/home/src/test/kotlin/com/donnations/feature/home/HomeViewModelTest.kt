package com.donnations.feature.home

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.donnations.core.base.testExceptionHandler
import com.donnations.core.model.Cached
import com.donnations.domain.home.model.Home
import com.donnations.domain.home.repository.HomeRepository
import com.donnations.feature.home.model.HomeUiMapper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
        repository.refreshGate = CompletableDeferred()

        viewModel.uiState.test {
            val state = awaitItem() as HomeUiState.Success
            assertEquals(listOf("f1"), state.home.featured.map { it.id })
        }
    }

    @Test
    fun `shows loading until the first refresh stores home`() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.refreshGate = gate

        viewModel.uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())

            gate.complete(Unit)

            assertTrue(awaitItem() is HomeUiState.Success)
        }
    }

    @Test
    fun `keeps showing the stored home when the refresh fails`() = runTest {
        repository.stored.value = Cached(home(), Instant.EPOCH)
        repository.failure = IOException("offline")

        viewModel.uiState.test {
            assertTrue(awaitItem() is HomeUiState.Success)
            assertEquals(1, repository.refreshes)
        }
    }

    @Test
    fun `emits error when the refresh fails with nothing stored and recovers on retry`() = runTest {
        repository.failure = IOException("offline")

        viewModel.uiState.test {
            assertTrue(awaitItem() is HomeUiState.Error)

            repository.failure = null
            viewModel.retry()

            // StateFlow may conflate the intermediate Loading.
            val next = awaitItem().let { if (it == HomeUiState.Loading) awaitItem() else it }
            assertTrue(next is HomeUiState.Success)
        }
    }

    @Test
    fun `selecting a category filters locally without refreshing again`() = runTest {
        viewModel.uiState.test {
            assertTrue(awaitItem() is HomeUiState.Success)

            viewModel.selectCategory("education")

            val state = awaitItem() as HomeUiState.Success
            assertEquals("education", state.home.selectedCategoryId)
            assertEquals(listOf("c2"), state.home.campaigns.map { it.id })
            assertEquals(1, repository.refreshes)
        }
    }

    private class FakeHomeRepository : HomeRepository {
        val stored = MutableStateFlow<Cached<Home>?>(null)
        var failure: Throwable? = null
        var refreshGate: CompletableDeferred<Unit>? = null
        var refreshes = 0

        override fun observeHome(): Flow<Cached<Home>?> = stored

        override suspend fun refreshHome() {
            refreshes++
            refreshGate?.await()
            failure?.let { throw it }
            stored.value = Cached(home(), Instant.EPOCH)
        }
    }
}

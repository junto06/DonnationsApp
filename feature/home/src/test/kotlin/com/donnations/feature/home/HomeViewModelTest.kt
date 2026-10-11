package com.donnations.feature.home

import app.cash.turbine.test
import com.donnations.domain.home.model.Home
import com.donnations.domain.home.repository.HomeRepository
import com.donnations.feature.home.model.HomeUiMapper
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

class HomeViewModelTest {

    private val repository = FakeHomeRepository()
    private val viewModel by lazy { HomeViewModel(repository, HomeUiMapper({ Locale.US }, testStrings)) }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `emits success with mapped home`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is HomeUiState.Success)
            assertEquals(listOf("f1"), (state as HomeUiState.Success).home.featured.map { it.id })
        }
    }

    @Test
    fun `emits error when loading fails and recovers on retry`() = runTest {
        repository.failure = IllegalStateException("offline")

        viewModel.uiState.test {
            assertTrue(awaitItem() is HomeUiState.Error)

            repository.failure = null
            viewModel.retry()

            // StateFlow may conflate the intermediate Loading.
            val next = awaitItem().let { if (it == HomeUiState.Loading) awaitItem() else it }
            assertTrue(next is HomeUiState.Success)
        }
    }

    private class FakeHomeRepository : HomeRepository {
        var failure: Throwable? = null

        override fun getHome(): Flow<Home> = flow {
            failure?.let { throw it }
            emit(home())
        }
    }
}

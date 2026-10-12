package com.donnations.core.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.donnations.core.base.RecordingLogger
import com.donnations.core.base.ReportingCoroutineExceptionHandler
import com.donnations.core.base.recordUnexpected
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ViewModelExceptionHandlingTest {

    private val logger = RecordingLogger()

    private class TestViewModel(override val exceptionHandler: CoroutineExceptionHandler) :
        ViewModel(), HasCoroutineExceptionHandler

    private val viewModel = TestViewModel(ReportingCoroutineExceptionHandler(logger))

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `an unhandled failure is reported and the view model scope keeps running`() {
        val bug = IllegalStateException("bug")

        viewModel.launchReporting { throw bug }

        assertEquals(listOf(bug), logger.recorded)
        assertTrue(viewModel.viewModelScope.isActive)
    }

    @Test
    fun `an unhandled connectivity failure is not reported, like everywhere else`() {
        viewModel.launchReporting { throw IOException("offline") }

        assertEquals(emptyList<Throwable>(), logger.recorded)
    }

    @Test
    fun `supervisorScopeWithReporting reports a failed async and rethrows it to the caller's catch`() = runTest {
        val bug = IllegalStateException("bug")

        val caught = try {
            viewModel.supervisorScopeWithReporting {
                val ok = async { 1 }
                val failing = async<Int> { throw bug }
                ok.await() + failing.await()
            }
            null
        } catch (e: IllegalStateException) {
            e
        }

        // Coroutines' debug-mode stack-trace recovery may hand over a copy, so compare by message, not identity.
        assertEquals("bug", caught?.message)
        assertEquals(listOf("bug"), logger.recorded.map { it.message })
    }

    @Test
    fun `a failed async caught inside the block doesn't cancel its siblings and isn't reported`() = runTest {
        val results = viewModel.supervisorScopeWithReporting {
            val ok = async { delay(100); "ok" }
            val failing = async<String> { throw IllegalStateException("one source down") }
            listOf(runCatching { failing.await() }.getOrDefault("fallback"), ok.await())
        }

        assertEquals(listOf("fallback", "ok"), results)
        assertEquals(emptyList<Throwable>(), logger.recorded)
    }

    @Test
    fun `a failed launch child is reported while the block still completes`() = runTest {
        val result = viewModel.supervisorScopeWithReporting {
            launch { throw IllegalStateException("background bug") }
            "done"
        }

        assertEquals("done", result)
        assertEquals(listOf("background bug"), logger.recorded.map { it.message })
    }

    @Test
    fun `supervisorScopeWithReporting returns the result and skips cancellation and connectivity`() = runTest {
        assertEquals(3, viewModel.supervisorScopeWithReporting { async { 1 }.await() + 2 })
        runCatching { viewModel.supervisorScopeWithReporting { throw CancellationException("left screen") } }
        runCatching { viewModel.supervisorScopeWithReporting { throw IOException("offline") } }

        assertEquals(emptyList<Throwable>(), logger.recorded)
    }

    @Test
    fun `a failure already reported by the data layer is not reported again when it escapes`() = runTest {
        val fromRepository = IllegalStateException("bad response")
        logger.recordUnexpected(fromRepository, "Loading thing failed") // what logged {} did

        runCatching { viewModel.supervisorScopeWithReporting { async<Int> { throw fromRepository }.await() } }

        assertEquals(listOf("bad response"), logger.recorded.map { it.message })
    }

    @Test
    fun `failures caught inside the block are not reported`() {
        viewModel.launchReporting { runCatching { error("handled") } }

        assertEquals(emptyList<Throwable>(), logger.recorded)
    }

    @Test
    fun `viewModelReportingScope carries the injected handler`() {
        assertEquals(viewModel.exceptionHandler, viewModel.viewModelReportingScope.coroutineContext[CoroutineExceptionHandler])
    }

    @Test
    fun `clearing the view model cancels the reporting scope and its coroutines`() {
        val store = ViewModelStore()
        val owned = ViewModelProvider.create(
            store,
            viewModelFactory { initializer { TestViewModel(ReportingCoroutineExceptionHandler(logger)) } },
        )[TestViewModel::class]
        val job = owned.launchReporting { awaitCancellation() }

        store.clear()

        assertTrue(job.isCancelled)
        assertFalse(owned.viewModelReportingScope.isActive)
    }
}

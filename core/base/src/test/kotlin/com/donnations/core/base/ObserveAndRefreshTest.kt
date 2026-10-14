package com.donnations.core.base

import app.cash.turbine.test
import com.donnations.core.model.Data
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ObserveAndRefreshTest {

    private val stored = MutableStateFlow<String?>(null)
    private val response = CompletableDeferred<String>()
    private var fetches = 0

    private val flow = observeAndRefresh(
        fromStorage = stored,
        fromNetwork = { fetches++; response.await() },
        toStorage = { stored.value = it },
    )

    @Test
    fun `emits the stored value while fetching, then the saved one`() = runTest {
        stored.value = "old"

        flow.test {
            assertEquals(Data(content = "old", loading = true), awaitItem())

            response.complete("new")

            assertEquals(Data(content = "new"), expectMostRecentItem())
        }
    }

    @Test
    fun `nothing stored emits loading while fetching`() = runTest {
        assertEquals(Data<String>(loading = true), flow.first())
    }

    @Test
    fun `a failed fetch keeps the stored value and carries the cause`() = runTest {
        stored.value = "old"
        val offline = IOException("offline")
        val failing = observeAndRefresh(stored, fromNetwork = { throw offline }, toStorage = { _: String -> })

        val settled = failing.first { it.error != null }

        assertEquals("old", settled.content)
        assertSame(offline, settled.error)
    }

    @Test
    fun `nothing stored and a failed fetch emits only the error`() = runTest {
        val offline = IOException("offline")
        val failing = observeAndRefresh(stored, fromNetwork = { throw offline }, toStorage = { _: String -> })

        val settled = failing.first { it.error != null }

        assertNull(settled.content)
        assertSame(offline, settled.error)
    }

    @Test
    fun `storage changes after the fetch still emit`() = runTest {
        response.complete("new")

        flow.test {
            assertEquals(Data(content = "new"), expectMostRecentItem())

            stored.value = "newer"

            assertEquals(Data(content = "newer"), awaitItem())
        }
    }

    @Test
    fun `a storage failure is emitted with the last value instead of thrown`() = runTest {
        val broken = IllegalStateException("bad row")
        val failingRead: Flow<String?> = flow {
            emit("old")
            throw broken
        }

        val emitted = observeAndRefresh(failingRead, fromNetwork = { awaitCancellation() }, toStorage = { _: String -> }).toList()

        assertEquals(Data(content = "old", loading = true), emitted.first())
        val failed = emitted.last()
        assertEquals("old", failed.content)
        assertFalse(failed.loading)
        // combine rethrows a debug-mode copy, so compare the failure by type and message, not identity.
        assertTrue(failed.error is IllegalStateException)
        assertEquals("bad row", failed.error?.message)
    }

    @Test
    fun `a new collection doesn't see the previous collection's value`() = runTest {
        var reads = 0
        val brokenOnSecondRead: Flow<String?> = flow {
            if (reads++ == 0) emit("old") else throw IllegalStateException("bad row")
        }
        val flow = observeAndRefresh(brokenOnSecondRead, fromNetwork = { awaitCancellation() }, toStorage = { _: String -> })

        flow.first()
        val second = flow.toList().single()

        assertNull(second.content)
    }

    @Test
    fun `every collection fetches once`() = runTest {
        val flow = observeAndRefresh(stored, fromNetwork = { "v${++fetches}" }, toStorage = { stored.value = it })

        flow.first { it.content == "v1" }
        flow.first { it.content == "v2" }

        assertEquals(2, fetches)
    }

    @Test
    fun `cancelling the collection cancels the in-flight fetch`() = runTest {
        val started = CompletableDeferred<Unit>()
        var cancelled = false
        val slow = observeAndRefresh(
            fromStorage = stored,
            fromNetwork = {
                started.complete(Unit)
                try {
                    awaitCancellation()
                } finally {
                    cancelled = true
                }
            },
            toStorage = { _: Unit -> },
        )

        val collection = launch { slow.collect { } }
        started.await()
        collection.cancelAndJoin()

        assertTrue(cancelled)
    }
}

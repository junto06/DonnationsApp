package com.donnations.core.base

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.IOException

class LoggedTest {

    private val recorded = mutableListOf<Pair<Throwable, String?>>()

    // A HasLogger in scope, like a repository implementing it.
    private val owner = object : HasLogger {
        override val logger = object : Logger {
            override fun debug(tag: String, message: String) = Unit
            override fun info(tag: String, message: String) = Unit
            override fun warn(tag: String, message: String, throwable: Throwable?) = Unit
            override fun error(tag: String, message: String, throwable: Throwable?) = Unit
            override fun recordException(throwable: Throwable, message: String?) {
                recorded += throwable to message
            }
        }
    }

    private suspend fun <T> call(isExpected: (Throwable) -> Boolean = { false }, block: () -> T): Result<T> =
        with(owner) { runCatching { logged("Loading thing", isExpected, block) } }

    @Test
    fun `returns the block result without recording`() = runTest {
        assertEquals(42, call { 42 }.getOrThrow())
        assertEquals(emptyList<Any>(), recorded)
    }

    @Test
    fun `records an unexpected failure with the operation name and rethrows it unchanged`() = runTest {
        val bug = IllegalStateException("bug")

        val result = call { throw bug }

        assertSame(bug, result.exceptionOrNull())
        assertEquals(listOf(bug to "Loading thing failed"), recorded)
    }

    @Test
    fun `does not record cancellation, connectivity or expected failures, but still rethrows them`() = runTest {
        listOf(CancellationException("left screen"), IOException("offline"), IllegalArgumentException("rejected"))
            .forEach { failure ->
                val result = call(isExpected = { it is IllegalArgumentException }) { throw failure }
                assertSame(failure, result.exceptionOrNull())
            }

        assertEquals(emptyList<Any>(), recorded)
    }
}

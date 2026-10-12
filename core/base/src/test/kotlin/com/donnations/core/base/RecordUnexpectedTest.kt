package com.donnations.core.base

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class RecordUnexpectedTest {

    private val recorded = mutableListOf<Pair<Throwable, String?>>()
    private val logger = object : Logger {
        override fun debug(tag: String, message: String) = Unit
        override fun info(tag: String, message: String) = Unit
        override fun warn(tag: String, message: String, throwable: Throwable?) = Unit
        override fun error(tag: String, message: String, throwable: Throwable?) = Unit
        override fun recordException(throwable: Throwable, message: String?) {
            recorded += throwable to message
        }
    }

    @Test
    fun `records unexpected failures with the message`() {
        val bug = IllegalStateException("bug")

        logger.recordUnexpected(bug, "Loading failed")

        assertEquals(listOf(bug to "Loading failed"), recorded)
    }

    @Test
    fun `skips connectivity failures, including subclasses`() {
        logger.recordUnexpected(IOException("offline"), "x")
        logger.recordUnexpected(SocketTimeoutException("timeout"), "x")

        assertEquals(emptyList<Pair<Throwable, String?>>(), recorded)
    }

    @Test
    fun `skips failures the caller marks as expected`() {
        logger.recordUnexpected(IllegalArgumentException("rejected"), "x") { it is IllegalArgumentException }

        assertEquals(emptyList<Pair<Throwable, String?>>(), recorded)
    }

    @Test
    fun `the same failure is recorded once, even through several reporting points`() {
        val bug = IllegalStateException("bug")

        logger.recordUnexpected(bug, "Loading failed")
        logger.recordUnexpected(bug, "Unhandled coroutine failure")

        assertEquals(listOf(bug to "Loading failed"), recorded)
    }

    @Before
    fun resetReported() {
        ReportedFailures.clear()
    }

    @Test
    fun `the original arriving after its copy is skipped too, so order doesn't matter`() {
        val original = IllegalStateException("bug")
        val copy = IllegalStateException("bug", original)

        logger.recordUnexpected(copy, "Unhandled coroutine failure")
        logger.recordUnexpected(original, "Loading failed")

        assertEquals(listOf(copy to "Unhandled coroutine failure"), recorded)
    }

    @Test
    fun `a copy or wrapper whose cause was already recorded is skipped`() {
        val original = IllegalStateException("bug")
        logger.recordUnexpected(original, "Loading failed")

        logger.recordUnexpected(IllegalStateException("bug", original), "Unhandled coroutine failure")
        logger.recordUnexpected(RuntimeException("wrapped", IllegalStateException("bug", original)), "Outer")

        assertEquals(listOf(original to "Loading failed"), recorded)
    }

    @Test
    fun `different failures are each recorded`() {
        val first = IllegalStateException("first")
        val second = IllegalStateException("second")

        logger.recordUnexpected(first, "a")
        logger.recordUnexpected(second, "b")

        assertEquals(listOf(first to "a", second to "b"), recorded)
    }
}

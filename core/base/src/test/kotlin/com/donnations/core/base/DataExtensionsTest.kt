package com.donnations.core.base

import com.donnations.core.model.Data
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class DataExtensionsTest {

    private val offline = IOException("offline")

    @Test
    fun `mapContent maps the content and keeps loading and error`() {
        assertEquals(Data(content = 2, error = offline, loading = true), Data(content = "ab", error = offline, loading = true).mapContent { it.length })
    }

    @Test
    fun `mapContent skips the block without content`() {
        assertEquals(Data<Int>(loading = true), Data<String>(loading = true).mapContent<String, Int> { error("not called") })
    }

    @Test
    fun `a failing mapContent block becomes an error instead of throwing`() = runTest {
        val broken = IllegalArgumentException("bad amount")

        val mapped = flowOf(Data(content = "a", loading = true), Data(content = "b")).mapContent { throw broken }.toList()

        assertEquals(listOf(Data<Nothing>(error = broken, loading = true), Data(error = broken)), mapped)
    }

    @Test
    fun `mapContent doesn't turn cancellation into an error`() = runTest {
        val result = runCatching { flowOf(Data(content = "a")).mapContent { throw CancellationException("left") }.first() }

        assertTrue(result.exceptionOrNull() is CancellationException)
    }

    @Test
    fun `fold shows content even while loading or after a failure`() {
        assertEquals("content a", Data(content = "a", error = offline, loading = true).render())
    }

    @Test
    fun `fold shows the error when nothing is stored`() {
        assertEquals("error offline", Data<String>(error = offline).render())
    }

    @Test
    fun `fold shows loading while nothing is stored yet`() {
        assertEquals("loading", Data<String>(loading = true).render())
    }

    @Test
    fun `fold turns finished with nothing stored into an error`() {
        assertEquals("error Nothing stored", Data<String>().render())
    }

    private fun Data<String>.render() =
        fold(onLoading = { "loading" }, onError = { "error ${it.message}" }, onContent = { "content $it" })
}

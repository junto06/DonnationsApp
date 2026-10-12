package com.donnations.data.home.repository

import com.donnations.core.base.RecordingLogger
import com.donnations.data.home.remote.HomeApi
import com.donnations.data.home.remote.dto.HomeDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.IOException

class HomeRepositoryImplTest {

    private val logger = RecordingLogger()

    private suspend fun loadFailingWith(failure: Throwable): Throwable? {
        val api = object : HomeApi {
            override suspend fun getHome(): HomeDto = throw failure
        }
        return runCatching { HomeRepositoryImpl(api, logger).getHome().first() }.exceptionOrNull()
    }

    @Test
    fun `reports an unexpected failure once and rethrows it unchanged`() = runTest {
        val bug = IllegalStateException("bad response")

        assertSame(bug, loadFailingWith(bug))
        assertEquals(listOf(bug), logger.recorded)
    }

    @Test
    fun `does not report connectivity failures`() = runTest {
        loadFailingWith(IOException("offline"))

        assertEquals(emptyList<Throwable>(), logger.recorded)
    }
}

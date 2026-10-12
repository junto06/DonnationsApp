package com.donnations.data.campaign.repository

import com.donnations.core.base.RecordingLogger
import com.donnations.core.model.CampaignId
import com.donnations.data.campaign.remote.CampaignApi
import com.donnations.data.campaign.remote.dto.CampaignDetailDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.IOException

class CampaignRepositoryImplTest {

    private val logger = RecordingLogger()

    private suspend fun loadFailingWith(failure: Throwable): Throwable? {
        val api = object : CampaignApi {
            override suspend fun getCampaign(id: String): CampaignDetailDto = throw failure
        }
        return runCatching { CampaignRepositoryImpl(api, logger).getCampaign(CampaignId("c1")).first() }.exceptionOrNull()
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

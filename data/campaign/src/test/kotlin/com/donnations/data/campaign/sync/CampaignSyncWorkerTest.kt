package com.donnations.data.campaign.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.donnations.core.base.RecordingLogger
import com.donnations.data.campaign.remote.FakeCampaignApi
import com.donnations.data.campaign.repository.CampaignRepositoryImpl
import com.donnations.data.campaign.sync.FakeSyncScheduler
import com.donnations.data.campaignstore.FakeCampaignDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@RunWith(RobolectricTestRunner::class)
class CampaignSyncWorkerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val api = FakeCampaignApi()
    private val dao = FakeCampaignDao()
    private val logger = RecordingLogger()
    private val repository = CampaignRepositoryImpl(api, dao, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), FakeSyncScheduler(), logger)

    @Test
    fun `stores the fetched campaign`() = runTest {
        assertEquals(ListenableWorker.Result.success(), sync("c1"))

        assertEquals("Care Clinics", dao.rows.value.getValue("c1").organizer)
    }

    @Test
    fun `fails when offline`() = runTest {
        api.failure = IOException("offline")

        assertEquals(ListenableWorker.Result.failure(), sync("c1"))
    }

    @Test
    fun `fails without retrying on a malformed response and keeps nothing`() = runTest {
        api.response = api.response.copy(raised = "not a number")

        assertEquals(ListenableWorker.Result.failure(), sync("c1"))
        assertNull(dao.rows.value["c1"])
    }

    @Test
    fun `fails without a campaign id`() = runTest {
        assertEquals(ListenableWorker.Result.failure(), sync(id = null))
        assertEquals(0, api.calls)
    }

    private suspend fun sync(id: String?): ListenableWorker.Result =
        TestListenableWorkerBuilder<CampaignSyncWorker>(context)
            .setInputData(if (id == null) workDataOf() else workDataOf(CampaignSyncWorker.KEY_CAMPAIGN_ID to id))
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters) =
                    CampaignSyncWorker(appContext, workerParameters, repository, logger)
            })
            .build()
            .doWork()
}

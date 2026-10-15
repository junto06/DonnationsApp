package com.donnations.data.campaign.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.donnations.core.model.CampaignId
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// Real WorkManager (test init) with no network, so enqueued syncs stay pending.
@RunWith(RobolectricTestRunner::class)
class WorkManagerSyncSchedulerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val id = CampaignId("c1")
    private lateinit var workManager: WorkManager
    private lateinit var scheduler: WorkManagerSyncScheduler

    @Before
    fun setUp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setExecutor(SynchronousExecutor()).build(),
        )
        workManager = WorkManager.getInstance(context)
        scheduler = WorkManagerSyncScheduler(workManager)
    }

    @Test
    fun `schedules one sync for the campaign that waits for network`() {
        scheduler.scheduleSync(id)

        val work = syncs(id).single()
        assertEquals(WorkInfo.State.ENQUEUED, work.state)
        assertEquals(NetworkType.CONNECTED, work.constraints.requiredNetworkType)
    }

    @Test
    fun `scheduling again while one is pending keeps the pending one`() {
        scheduler.scheduleSync(id)
        val pending = syncs(id).single().id

        scheduler.scheduleSync(id)

        assertEquals(pending, syncs(id).single().id)
    }

    @Test
    fun `campaigns sync independently`() {
        scheduler.scheduleSync(id)
        scheduler.scheduleSync(CampaignId("c2"))

        assertEquals(1, syncs(id).size)
        assertEquals(1, syncs(CampaignId("c2")).size)
    }

    private fun syncs(id: CampaignId): List<WorkInfo> =
        workManager.getWorkInfosForUniqueWork("campaign-sync-${id.value}").get()
}

package com.donnations.data.campaign.sync

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.donnations.core.model.CampaignId
import com.donnations.domain.campaign.sync.SyncScheduler
import javax.inject.Inject

// One sync per campaign at a time, run once online
// it survives app restarts and reboots.
class WorkManagerSyncScheduler @Inject constructor(
    private val workManager: WorkManager,
) : SyncScheduler {

    override fun scheduleSync(id: CampaignId) {
        val constraints = Constraints
            .Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val request = OneTimeWorkRequestBuilder<CampaignSyncWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(CampaignSyncWorker.KEY_CAMPAIGN_ID to id.value))
            .build()
        workManager.enqueueUniqueWork(workName(id), ExistingWorkPolicy.KEEP, request)
    }

    private fun workName(id: CampaignId) = "campaign-sync-${id.value}"
}

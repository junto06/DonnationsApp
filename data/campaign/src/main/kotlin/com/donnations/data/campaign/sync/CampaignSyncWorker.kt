package com.donnations.data.campaign.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.donnations.core.base.Logger
import com.donnations.core.model.CampaignId
import com.donnations.data.campaign.repository.CampaignRepositoryImpl
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import java.io.IOException

// Refreshes one stored campaign in the background.
@HiltWorker
class CampaignSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: CampaignRepositoryImpl,
    private val logger: Logger,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getString(KEY_CAMPAIGN_ID) ?: return Result.failure()
        return try {
            repository.refresh(CampaignId(id))
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error("CampaignSyncWorker", "Error occurred while syncing campaign: $id", e)
            Result.failure()
        }
    }

    internal companion object {
        const val KEY_CAMPAIGN_ID = "campaignId"
    }
}

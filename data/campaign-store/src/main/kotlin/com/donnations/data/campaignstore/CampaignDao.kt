package com.donnations.data.campaignstore

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CampaignDao {

    @Query("SELECT * FROM campaigns WHERE id = :id")
    abstract fun observeCampaign(id: String): Flow<CampaignEntity?>

    @Query("SELECT * FROM campaigns WHERE id IN (:ids)")
    abstract fun observeCampaigns(ids: List<String>): Flow<List<CampaignEntity>>

    @Upsert
    abstract suspend fun upsertCampaign(campaign: CampaignEntity)

    // Insert new campaigns, then update the shared columns of existing ones (minSdk 24's SQLite has no ON CONFLICT DO UPDATE).
    @Transaction
    open suspend fun upsertSummaries(summaries: List<CampaignSummaryEntity>) {
        insertNewSummaries(summaries)
        updateSummaries(summaries)
    }

    @Insert(entity = CampaignEntity::class, onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertNewSummaries(summaries: List<CampaignSummaryEntity>)

    @Update(entity = CampaignEntity::class)
    protected abstract suspend fun updateSummaries(summaries: List<CampaignSummaryEntity>)
}

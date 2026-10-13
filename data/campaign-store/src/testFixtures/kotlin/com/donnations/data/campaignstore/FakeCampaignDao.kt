package com.donnations.data.campaignstore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

// In-memory stand-in for the Room DAO; upsertSummaries() runs the real DAO logic on top of it.
class FakeCampaignDao : CampaignDao() {
    val rows = MutableStateFlow(emptyMap<String, CampaignEntity>())

    override fun observeCampaign(id: String): Flow<CampaignEntity?> = rows.map { it[id] }

    override fun observeCampaigns(ids: List<String>): Flow<List<CampaignEntity>> =
        rows.map { stored -> ids.mapNotNull { stored[it] } }

    override suspend fun upsertCampaign(campaign: CampaignEntity) {
        rows.value += campaign.id to campaign
    }

    override suspend fun insertNewSummaries(summaries: List<CampaignSummaryEntity>) {
        rows.value += summaries.filter { it.id !in rows.value }
            .associate { it.id to it.toEntity(description = null, organizer = null) }
    }

    override suspend fun updateSummaries(summaries: List<CampaignSummaryEntity>) {
        rows.value += summaries.mapNotNull { summary ->
            rows.value[summary.id]?.let { summary.id to summary.toEntity(it.description, it.organizer) }
        }
    }

    private fun CampaignSummaryEntity.toEntity(description: String?, organizer: String?) = CampaignEntity(
        id = id,
        title = title,
        summary = summary,
        location = location,
        categoryId = categoryId,
        imageUrl = imageUrl,
        currency = currency,
        goal = goal,
        raised = raised,
        donorCount = donorCount,
        endsAtEpochMillis = endsAtEpochMillis,
        description = description,
        organizer = organizer,
        updatedAtEpochMillis = updatedAtEpochMillis,
    )
}

package com.donnations.data.home.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// In-memory stand-in for the Room DAO; load() and replace() run the real DAO logic on top of it.
class FakeHomeDao : HomeDao() {
    private val snapshot = MutableStateFlow<HomeSnapshotEntity?>(null)
    private var categories = emptyList<CategoryEntity>()
    private var campaigns = emptyList<CampaignEntity>()

    override fun observeSnapshot(): Flow<HomeSnapshotEntity?> = snapshot
    override suspend fun snapshot(): HomeSnapshotEntity? = snapshot.value
    override suspend fun categories(): List<CategoryEntity> = categories.sortedBy { it.position }
    override suspend fun campaigns(): List<CampaignEntity> = campaigns
    override suspend fun upsertSnapshot(snapshot: HomeSnapshotEntity) {
        this.snapshot.value = snapshot
    }
    override suspend fun insertCategories(categories: List<CategoryEntity>) {
        this.categories += categories
    }
    override suspend fun insertCampaigns(campaigns: List<CampaignEntity>) {
        this.campaigns += campaigns
    }
    override suspend fun deleteCategories() {
        categories = emptyList()
    }
    override suspend fun deleteCampaigns() {
        campaigns = emptyList()
    }
}

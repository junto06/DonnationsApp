package com.donnations.backend.data.campaigns

import com.donnations.backend.domain.model.Campaign
import com.donnations.backend.domain.model.CampaignId
import com.donnations.backend.domain.repository.CampaignsRepository
import org.springframework.stereotype.Repository

@Repository
class CampaignsRepositoryImpl(
    private val campaignsStore: CampaignsStore,
) : CampaignsRepository {
    override fun getAll(): List<Campaign> = campaignsStore.getAll()

    override fun getById(id: CampaignId): Campaign? = campaignsStore.get(id.value)
}

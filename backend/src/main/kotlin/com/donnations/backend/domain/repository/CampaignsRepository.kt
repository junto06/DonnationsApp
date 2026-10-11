package com.donnations.backend.domain.repository

import com.donnations.backend.domain.model.Campaign
import com.donnations.backend.domain.model.CampaignId

interface CampaignsRepository {
    fun getAll(): List<Campaign>

    fun getById(id: CampaignId): Campaign?

    // Atomic read-modify-write so concurrent donations never lose an update
    // null if the campaign is missing.
    fun update(id: CampaignId, transform: (Campaign) -> Campaign): Campaign?
}

package com.donnations.backend.domain.repository

import com.donnations.backend.domain.model.Campaign
import com.donnations.backend.domain.model.CampaignId

interface CampaignsRepository {
    fun getAll(): List<Campaign>

    fun getById(id: CampaignId): Campaign?

    // Atomic read-modify-write; null if the campaign is missing. If transform throws, the campaign stays
    // unchanged and the exception propagates: donation rejections rely on this, so every store must keep it.
    fun update(id: CampaignId, transform: (Campaign) -> Campaign): Campaign?
}

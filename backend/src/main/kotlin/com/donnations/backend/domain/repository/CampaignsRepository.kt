package com.donnations.backend.domain.repository

import com.donnations.backend.domain.model.Campaign
import com.donnations.backend.domain.model.CampaignId

interface CampaignsRepository {
    fun getAll(): List<Campaign>

    fun getById(id: CampaignId): Campaign?
}

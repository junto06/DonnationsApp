package com.donnations.domain.campaign.repository

import com.donnations.core.model.CampaignId
import com.donnations.core.model.Cached
import com.donnations.core.model.Data
import com.donnations.domain.campaign.model.CampaignDetail
import kotlinx.coroutines.flow.Flow

interface CampaignRepository {
    // The stored campaign right away (also one stored by home), then refreshed from the backend (once per collection). Never throws.
    fun getCampaign(id: CampaignId): Flow<Data<Cached<CampaignDetail>>>
}

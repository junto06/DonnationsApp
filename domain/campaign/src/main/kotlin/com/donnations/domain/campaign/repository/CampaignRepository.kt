package com.donnations.domain.campaign.repository

import com.donnations.core.model.CampaignId
import com.donnations.domain.campaign.model.CampaignDetail
import kotlinx.coroutines.flow.Flow

interface CampaignRepository {
    fun getCampaign(id: CampaignId): Flow<CampaignDetail>
}

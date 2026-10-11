package com.donnations.domain.campaign.repository

import com.donnations.domain.campaign.model.CampaignDetail
import kotlinx.coroutines.flow.Flow

interface CampaignRepository {
    fun getCampaign(id: String): Flow<CampaignDetail>
}

package com.donnations.data.campaign.repository

import com.donnations.core.model.CampaignId
import com.donnations.data.campaign.remote.CampaignApi
import com.donnations.data.campaign.remote.toDomain
import com.donnations.domain.campaign.model.CampaignDetail
import com.donnations.domain.campaign.repository.CampaignRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CampaignRepositoryImpl @Inject constructor(
    private val api: CampaignApi,
) : CampaignRepository {
    override fun getCampaign(id: CampaignId): Flow<CampaignDetail> = flow {
        emit(api.getCampaign(id.value).toDomain())
    }
}

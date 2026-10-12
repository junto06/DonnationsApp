package com.donnations.data.campaign.repository

import com.donnations.core.base.HasLogger
import com.donnations.core.base.Logger
import com.donnations.core.base.logged
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
    override val logger: Logger,
) : CampaignRepository, HasLogger {
    override fun getCampaign(id: CampaignId): Flow<CampaignDetail> = flow {
        val campaigns = logged("Loading campaign") {
            api.getCampaign(id.value).toDomain()
        }
        emit(campaigns)
    }
}

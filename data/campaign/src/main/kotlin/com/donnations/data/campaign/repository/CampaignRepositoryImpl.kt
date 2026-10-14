package com.donnations.data.campaign.repository

import com.donnations.core.base.HasLogger
import com.donnations.core.base.Logger
import com.donnations.core.base.logged
import com.donnations.core.base.observeAndRefresh
import com.donnations.core.model.Cached
import com.donnations.core.model.CampaignId
import com.donnations.core.model.Data
import com.donnations.data.campaign.remote.CampaignApi
import com.donnations.data.campaign.remote.toDomain
import com.donnations.data.campaign.remote.toEntity
import com.donnations.data.campaignstore.CampaignDao
import com.donnations.domain.campaign.model.CampaignDetail
import com.donnations.domain.campaign.repository.CampaignRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

class CampaignRepositoryImpl @Inject constructor(
    private val api: CampaignApi,
    private val dao: CampaignDao,
    private val clock: Clock,
    override val logger: Logger,
) : CampaignRepository, HasLogger {

    override fun getCampaign(id: CampaignId): Flow<Data<Cached<CampaignDetail>>> =
        observeAndRefresh(
            fromStorage = dao.observeCampaign(id.value).map { stored ->
                logged("Reading stored campaign") {
                    stored?.toDomain()
                }
            },
            // Mapped before storing, so a malformed response never replaces a good copy
            fromNetwork = {
                logged("Refreshing campaign") {
                    api.getCampaign(id.value).toEntity(clock.instant())
                }
            },
            toStorage = { campaign -> dao.upsertCampaign(campaign) },
        )
}

package com.donnations.data.campaign.sync

import com.donnations.core.model.CampaignId
import com.donnations.domain.campaign.sync.SyncScheduler

class FakeSyncScheduler : SyncScheduler {
    val scheduled = mutableListOf<CampaignId>()

    override fun scheduleSync(id: CampaignId) {
        scheduled += id
    }
}

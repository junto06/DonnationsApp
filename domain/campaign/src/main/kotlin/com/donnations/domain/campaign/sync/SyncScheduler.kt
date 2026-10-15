package com.donnations.domain.campaign.sync

import com.donnations.core.model.CampaignId

interface SyncScheduler {
    fun scheduleSync(id: CampaignId)
}

package com.donnations.backend.data.campaigns

import com.donnations.backend.domain.model.Campaign
import java.util.UUID

interface CampaignsStore {
    fun getAll(): List<Campaign>

    fun get(id: UUID): Campaign?

    fun applyDonation(id: UUID, donationId: UUID, transform: (Campaign) -> Campaign): Campaign?
}

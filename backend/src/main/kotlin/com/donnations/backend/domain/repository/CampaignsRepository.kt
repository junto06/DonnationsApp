package com.donnations.backend.domain.repository

import com.donnations.backend.domain.model.Campaign
import com.donnations.backend.domain.model.CampaignId
import com.donnations.backend.domain.model.DonationId

interface CampaignsRepository {
    fun getAll(): List<Campaign>

    fun getById(id: CampaignId): Campaign?

    // Atomically applies a donation at most once: a repeat for the same donationId returns the campaign unchanged
    // without calling transform, so a retried donation never counts twice. Null if the campaign is missing.
    fun applyDonation(id: CampaignId, donationId: DonationId, transform: (Campaign) -> Campaign): Campaign?
}

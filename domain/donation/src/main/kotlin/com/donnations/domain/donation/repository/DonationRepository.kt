package com.donnations.domain.donation.repository

import com.donnations.core.model.CampaignId
import com.donnations.domain.donation.model.DonationOptions
import kotlinx.coroutines.flow.Flow

interface DonationRepository {
    // Throws CampaignEndedException / CampaignNotFoundException, or any network error.
    fun getDonationOptions(campaignId: CampaignId): Flow<DonationOptions>
}

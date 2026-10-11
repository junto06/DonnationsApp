package com.donnations.domain.donation.repository

import com.donnations.core.model.CampaignId
import com.donnations.domain.donation.model.Donation
import com.donnations.domain.donation.model.DonationOptions
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface DonationRepository {
    fun getDonationOptions(campaignId: CampaignId): Flow<DonationOptions>

    // amount is in the campaign's currency (major units).
    suspend fun donate(campaignId: CampaignId, amount: BigDecimal): Donation
}

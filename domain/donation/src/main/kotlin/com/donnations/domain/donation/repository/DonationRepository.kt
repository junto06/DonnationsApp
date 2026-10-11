package com.donnations.domain.donation.repository

import com.donnations.core.model.CampaignId
import com.donnations.core.model.DonationId
import com.donnations.domain.donation.model.Donation
import com.donnations.domain.donation.model.DonationOptions
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface DonationRepository {
    fun getDonationOptions(campaignId: CampaignId): Flow<DonationOptions>

    // Submits the donation and returns it with its current status, without waiting for processing. id is the idempotency
    // key: reuse it to retry the same attempt safely. amount is in the campaign's currency (major units).
    suspend fun submitDonation(id: DonationId, campaignId: CampaignId, amount: BigDecimal): Donation

    // Current state of a submitted donation; throws the exception for its failure code once processing has failed.
    suspend fun getDonation(id: DonationId): Donation
}

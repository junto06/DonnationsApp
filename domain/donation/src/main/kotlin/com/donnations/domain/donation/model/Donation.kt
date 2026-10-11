package com.donnations.domain.donation.model

import com.donnations.core.model.CampaignId
import com.donnations.core.model.DonationId
import java.math.BigDecimal
import java.time.Instant

// The client only needs to know whether a submitted donation has finished; a failed one is reported as a thrown exception.
enum class DonationStatus { IN_PROGRESS, COMPLETED }

data class Donation(
    val id: DonationId,
    val campaignId: CampaignId,
    val amount: BigDecimal,
    val currencyCode: String,
    val createdAt: Instant,
    val status: DonationStatus,
)

package com.donnations.domain.donation.model

import com.donnations.core.model.CampaignId
import com.donnations.core.model.DonationId
import java.math.BigDecimal
import java.time.Instant

data class Donation(
    val id: DonationId,
    val campaignId: CampaignId,
    val amount: BigDecimal,
    val currencyCode: String,
    val createdAt: Instant,
)

package com.donnations.domain.donation.model

import com.donnations.core.model.CampaignId
import java.math.BigDecimal

data class DonationOptions(
    val campaignId: CampaignId,
    val campaignTitle: String,
    val currencyCode: String,
    val suggestedAmounts: List<BigDecimal>,
)

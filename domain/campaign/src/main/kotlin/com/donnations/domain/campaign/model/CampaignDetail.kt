package com.donnations.domain.campaign.model

import com.donnations.core.model.CampaignId
import java.math.BigDecimal
import java.time.Instant

data class CampaignDetail(
    val id: CampaignId,
    val title: String,
    val summary: String,
    val description: String,
    val organizer: String,
    val location: String,
    val imageUrl: String,
    val currencyCode: String,
    val goal: BigDecimal,
    val raised: BigDecimal,
    val donorCount: Int,
    val endsAt: Instant,
)

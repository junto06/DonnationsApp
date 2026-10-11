package com.donnations.domain.home.model

import com.donnations.core.model.CampaignId
import java.math.BigDecimal

data class Campaign(
    val id: CampaignId,
    val title: String,
    val summary: String,
    val location: String,
    val categoryId: String,
    val imageUrl: String,
    val currencyCode: String,
    val goal: BigDecimal,
    val raised: BigDecimal,
)

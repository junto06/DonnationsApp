package com.donnations.data.donation.remote

import com.donnations.core.model.CampaignId
import com.donnations.data.donation.remote.dto.DonationOptionsDto
import com.donnations.domain.donation.model.DonationOptions

fun DonationOptionsDto.toDomain(): DonationOptions = DonationOptions(
    campaignId = CampaignId(campaignId),
    campaignTitle = campaignTitle,
    currencyCode = currency,
    suggestedAmounts = suggestedAmounts.map { it.toBigDecimal() },
)

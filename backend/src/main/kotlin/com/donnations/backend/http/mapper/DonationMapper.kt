package com.donnations.backend.http.mapper

import com.donnations.backend.domain.model.DonationOptions
import com.donnations.backend.http.dto.DonationOptionsDto

fun DonationOptions.toDto(): DonationOptionsDto = DonationOptionsDto(
    campaignId = campaign.id.value,
    campaignTitle = campaign.title,
    currency = campaign.currency.code,
    suggestedAmounts = suggestedAmounts.map { it.amount.toPlainString() },
)

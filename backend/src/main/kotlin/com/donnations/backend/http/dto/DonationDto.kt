package com.donnations.backend.http.dto

import java.util.UUID

data class DonationOptionsDto(
    val campaignId: UUID,
    val campaignTitle: String,
    val currency: String,
    val suggestedAmounts: List<String>, // decimal strings in major units ("25.00") in the campaign's currency.
)

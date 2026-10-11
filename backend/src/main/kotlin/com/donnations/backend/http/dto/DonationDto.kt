package com.donnations.backend.http.dto

import java.time.Instant
import java.util.UUID

data class DonationRequestDto(
    val amount: String, // decimal string in major units ("25.00") in the campaign's currency.
)

data class DonationDto(
    val id: UUID,
    val campaignId: UUID,
    val currency: String,
    val amount: String, // decimal string in major units ("25.00").
    val createdAt: Instant,
)

data class DonationOptionsDto(
    val campaignId: UUID,
    val campaignTitle: String,
    val currency: String,
    val suggestedAmounts: List<String>, // decimal strings in major units ("25.00") in the campaign's currency.
)

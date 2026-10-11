package com.donnations.data.donation.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// suggestedAmounts are decimal strings in major units ("25.00"); the mapper turns them into BigDecimal, never Double.
@Serializable
data class DonationOptionsDto(
    @SerialName("campaignId") val campaignId: String,
    @SerialName("campaignTitle") val campaignTitle: String,
    @SerialName("currency") val currency: String,
    @SerialName("acceptingDonations") val acceptingDonations: Boolean,
    @SerialName("suggestedAmounts") val suggestedAmounts: List<String>,
)

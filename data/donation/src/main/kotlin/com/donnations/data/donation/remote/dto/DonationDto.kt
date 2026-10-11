package com.donnations.data.donation.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DonationRequestDto(
    @SerialName("amount") val amount: String, // decimal string in major units ("25.00")
)

@Serializable
data class DonationDto(
    @SerialName("id") val id: String,
    @SerialName("campaignId") val campaignId: String,
    @SerialName("currency") val currency: String,
    @SerialName("amount") val amount: String, // decimal string in major units ("25.00")
    @SerialName("createdAt") val createdAt: String,
)

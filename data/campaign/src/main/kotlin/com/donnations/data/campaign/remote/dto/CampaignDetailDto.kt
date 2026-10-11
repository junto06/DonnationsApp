package com.donnations.data.campaign.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CampaignDetailDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("summary") val summary: String,
    @SerialName("description") val description: String,
    @SerialName("organizer") val organizer: String,
    @SerialName("location") val location: String,
    @SerialName("imageUrl") val imageUrl: String,
    @SerialName("currency") val currency: String,
    @SerialName("goal") val goal: String,
    @SerialName("raised") val raised: String,
    @SerialName("donorCount") val donorCount: Int,
    @SerialName("endsAt") val endsAt: String,
)

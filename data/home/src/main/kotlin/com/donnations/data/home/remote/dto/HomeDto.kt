package com.donnations.data.home.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeDto(
    @SerialName("categories") val categories: List<CategoryDto>,
    @SerialName("featured") val featured: List<CampaignDto>,
    @SerialName("campaigns") val campaigns: List<CampaignDto>,
)

@Serializable
data class CategoryDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
)

// goal/raised are decimal strings in major units ("8210.00"), kept as BigDecimal to avoid float rounding.
@Serializable
data class CampaignDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("summary") val summary: String,
    @SerialName("location") val location: String,
    @SerialName("categoryId") val categoryId: String,
    @SerialName("imageUrl") val imageUrl: String,
    @SerialName("currency") val currency: String,
    @SerialName("goal") val goal: String,
    @SerialName("raised") val raised: String,
)

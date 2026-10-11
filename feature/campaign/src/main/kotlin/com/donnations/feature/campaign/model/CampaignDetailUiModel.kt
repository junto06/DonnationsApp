package com.donnations.feature.campaign.model

data class CampaignDetailUiModel(
    val title: String,
    val summary: String,
    val description: String,
    val organizer: String,
    val location: String,
    val imageUrl: String,
    val raised: String,
    val goal: String,
    val percent: String,
    val progress: Float,
    val donorCount: String,
    val endsAt: String,
)

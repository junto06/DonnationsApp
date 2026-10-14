package com.donnations.feature.campaign.model

data class CampaignDetailUiModel(
    val title: String,
    val summary: String,
    // null/hidden until the campaign's detail is fetched
    // a campaign stored by home doesn't have them yet
    val description: String?,
    val organizer: String?,
    val location: String,
    val imageUrl: String,
    val raised: String,
    val goal: String,
    val percent: String,
    val progress: Float,
    val donorCount: String,
    val endsAt: String,
)

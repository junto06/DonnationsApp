package com.donnations.feature.home.model

data class HomeUiModel(
    val categories: List<CategoryUiModel>,
    val featured: List<CampaignUiModel>,
    val campaigns: List<CampaignUiModel>,
)

data class CategoryUiModel(
    val id: String,
    val name: String,
)

data class CampaignUiModel(
    val id: String,
    val title: String,
    val summary: String,
    val location: String,
    val imageUrl: String,
    val raised: String,
    val goal: String,
    val percent: String,
    val progress: Float,
)

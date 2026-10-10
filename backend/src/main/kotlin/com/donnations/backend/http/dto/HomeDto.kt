package com.donnations.backend.http.dto

data class HomeDto(
    val categories: List<CategoryDto>,
    val featured: List<CampaignSummaryDto>,
    val campaigns: List<CampaignSummaryDto>,
)

data class CategoryDto(
    val id: String,
    val name: String,
)

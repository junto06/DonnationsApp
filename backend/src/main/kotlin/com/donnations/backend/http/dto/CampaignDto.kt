package com.donnations.backend.http.dto

import java.time.Instant
import java.util.UUID

// Amounts are decimal strings in major units ("32750.00") so clients never handle fraction digits or floats.
data class CampaignSummaryDto(
    val id: UUID,
    val title: String,
    val summary: String,
    val imageUrl: String,
    val currency: String,
    val goal: String,
    val raised: String,
    val donorCount: Int,
    val endsAt: Instant,
)

data class CampaignDetailDto(
    val id: UUID,
    val title: String,
    val summary: String,
    val description: String,
    val organizer: String,
    val imageUrl: String,
    val currency: String,
    val goal: String,
    val raised: String,
    val donorCount: Int,
    val createdAt: Instant,
    val endsAt: Instant,
)

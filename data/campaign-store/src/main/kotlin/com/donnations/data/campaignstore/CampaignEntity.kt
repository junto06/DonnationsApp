package com.donnations.data.campaignstore

import androidx.room.Entity
import androidx.room.PrimaryKey

// Campaigns stored for every screen that shows them (home lists, detail).
// goal/raised stay plain decimal strings to keep exact amounts.
@Entity(tableName = "campaigns")
data class CampaignEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val location: String,
    val categoryId: String,
    val imageUrl: String,
    val currency: String,
    val goal: String,
    val raised: String,
    val donorCount: Int,
    val endsAtEpochMillis: Long,
    // optional until the campaign's detail is fetched
    val description: String?,
    val organizer: String?,
    val updatedAtEpochMillis: Long,
)

// The fields every campaign response has
// saving one keeps a stored description/organizer.
data class CampaignSummaryEntity(
    val id: String,
    val title: String,
    val summary: String,
    val location: String,
    val categoryId: String,
    val imageUrl: String,
    val currency: String,
    val goal: String,
    val raised: String,
    val donorCount: Int,
    val endsAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

package com.donnations.backend.domain.model

import java.time.Instant
import java.util.UUID

@JvmInline
value class CampaignId(val value: UUID)

data class Campaign(
    val id: CampaignId,
    val title: String,
    val summary: String,
    val description: String,
    val organizer: String,
    val location: String,
    val categoryId: CategoryId,
    val imageUrl: String,
    val goal: Money,
    val raised: Money,
    val donorCount: Int,
    val featured: Boolean,
    val createdAt: Instant,
    val endsAt: Instant,
) {
    init {
        require(title.isNotBlank()) { "Campaign title must not be blank" }
        require(goal.amountMinor > 0) { "Campaign goal must be positive" }
        require(raised.currency == goal.currency) { "Raised and goal must share a currency" }
        require(donorCount >= 0) { "Donor count cannot be negative" }
        require(endsAt.isAfter(createdAt)) { "Campaign must end after it starts" }
    }
}

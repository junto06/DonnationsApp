package com.donnations.backend.domain

import com.donnations.backend.domain.model.Campaign
import com.donnations.backend.domain.model.CampaignId
import com.donnations.backend.domain.model.CategoryId
import com.donnations.backend.domain.model.Currency
import com.donnations.backend.domain.model.Money
import java.time.Instant
import java.util.UUID

fun campaign(
    id: CampaignId = CampaignId(UUID.randomUUID()),
    title: String = "Campaign",
    goal: Money = Money(10_000, Currency.EUR),
    raised: Money = Money(2_500, Currency.EUR),
    donorCount: Int = 3,
    featured: Boolean = false,
    categoryId: CategoryId = CategoryId("education"),
    createdAt: Instant = Instant.parse("2026-10-01T00:00:00Z"),
    endsAt: Instant = Instant.parse("2026-12-01T00:00:00Z"),
) = Campaign(
    id = id,
    title = title,
    summary = "Summary",
    description = "Description",
    organizer = "Organizer",
    location = "Lahore, Pakistan",
    categoryId = categoryId,
    imageUrl = "https://example.com/image.jpg",
    goal = goal,
    raised = raised,
    donorCount = donorCount,
    featured = featured,
    createdAt = createdAt,
    endsAt = endsAt,
)

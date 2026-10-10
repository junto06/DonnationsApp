package com.donnations.backend.domain

import com.donnations.backend.domain.model.Campaign
import com.donnations.backend.domain.model.CampaignId
import com.donnations.backend.domain.model.Currency
import com.donnations.backend.domain.model.Money
import java.time.Instant
import java.util.UUID

fun campaign(
    id: CampaignId = CampaignId(UUID.randomUUID()),
    title: String = "Campaign",
    goal: Money = Money(10_000, Currency.USD),
    raised: Money = Money(2_500, Currency.USD),
    donorCount: Int = 3,
    createdAt: Instant = Instant.parse("2026-10-01T00:00:00Z"),
    endsAt: Instant = Instant.parse("2026-12-01T00:00:00Z"),
) = Campaign(
    id = id,
    title = title,
    summary = "Summary",
    description = "Description",
    organizer = "Organizer",
    imageUrl = "https://example.com/image.jpg",
    goal = goal,
    raised = raised,
    donorCount = donorCount,
    createdAt = createdAt,
    endsAt = endsAt,
)

package com.donnations.backend.data.campaigns.store

import com.donnations.backend.data.campaigns.CampaignsStore
import com.donnations.backend.data.categories.store.SeedCategoryIds
import com.donnations.backend.domain.model.Campaign
import com.donnations.backend.domain.model.CampaignId
import com.donnations.backend.domain.model.Currency
import com.donnations.backend.domain.model.Money
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Component

// Seeded placeholder until a database is added
// replace with a JPA-backed store
@Component
class InMemoryCampaignsStore(
    campaigns: List<Campaign> = seedCampaigns,
) : CampaignsStore {

    private val campaigns = campaigns.associateBy { it.id.value }

    override fun getAll(): List<Campaign> = campaigns.values.toList()

    override fun get(id: UUID): Campaign? = campaigns[id]
}

private val seedCampaigns = listOf(
    Campaign(
        id = CampaignId(UUID.fromString("3f1c2a5e-8d4b-4f0a-9c6e-1a2b3c4d5e01")),
        title = "Clean Water for Rural Schools",
        summary = "Install water filters in 20 village schools.",
        description = "Thousands of children walk miles for water every day. Each filter serves a school for five years.",
        organizer = "WaterBridge Foundation",
        location = "Sindh, Pakistan",
        categoryId = SeedCategoryIds.water,
        imageUrl = "https://images.donnations.com/campaigns/clean-water.jpg",
        goal = Money(5_000_000, Currency.EUR),
        raised = Money(3_275_000, Currency.EUR),
        donorCount = 412,
        featured = false,
        createdAt = Instant.parse("2026-09-01T09:00:00Z"),
        endsAt = Instant.parse("2026-12-31T23:59:59Z"),
    ),
    Campaign(
        id = CampaignId(UUID.fromString("3f1c2a5e-8d4b-4f0a-9c6e-1a2b3c4d5e02")),
        title = "Books for Every Child",
        summary = "Stock 50 community libraries with children's books.",
        description = "A shelf of books changes what a child believes is possible. We partner with local librarians to choose titles.",
        organizer = "ReadTogether",
        location = "Nairobi, Kenya",
        categoryId = SeedCategoryIds.education,
        imageUrl = "https://images.donnations.com/campaigns/books.jpg",
        goal = Money(2_000_000, Currency.EUR),
        raised = Money(640_000, Currency.EUR),
        donorCount = 158,
        featured = false,
        createdAt = Instant.parse("2026-09-20T09:00:00Z"),
        endsAt = Instant.parse("2027-03-31T23:59:59Z"),
    ),
    Campaign(
        id = CampaignId(UUID.fromString("3f1c2a5e-8d4b-4f0a-9c6e-1a2b3c4d5e03")),
        title = "Emergency Flood Relief",
        summary = "Food, shelter and medical kits for displaced families.",
        description = "Recent floods displaced over 10,000 families. Every donation goes directly to relief kits on the ground.",
        organizer = "Relief Now",
        location = "Punjab, Pakistan",
        categoryId = SeedCategoryIds.emergency,
        imageUrl = "https://images.donnations.com/campaigns/flood-relief.jpg",
        goal = Money(10_000_000, Currency.EUR),
        raised = Money(8_910_000, Currency.EUR),
        donorCount = 2_031,
        featured = true,
        createdAt = Instant.parse("2026-10-02T09:00:00Z"),
        endsAt = Instant.parse("2026-11-15T23:59:59Z"),
    ),
    Campaign(
        id = CampaignId(UUID.fromString("3f1c2a5e-8d4b-4f0a-9c6e-1a2b3c4d5e04")),
        title = "Help Children Get Back to School",
        summary = "Education changes lives. Support school supplies for children in need.",
        description = "Many children in underprivileged communities lack access to basic education supplies. Your donation helps provide school bags, books and uniforms.",
        organizer = "Hope Schools Trust",
        location = "Lahore, Pakistan",
        categoryId = SeedCategoryIds.education,
        imageUrl = "https://images.donnations.com/campaigns/back-to-school.jpg",
        goal = Money(800_000, Currency.EUR),
        raised = Money(395_000, Currency.EUR),
        donorCount = 120,
        featured = true,
        createdAt = Instant.parse("2026-09-25T09:00:00Z"),
        endsAt = Instant.parse("2026-11-30T23:59:59Z"),
    ),
    Campaign(
        id = CampaignId(UUID.fromString("3f1c2a5e-8d4b-4f0a-9c6e-1a2b3c4d5e05")),
        title = "Medical Support for Flood Victims",
        summary = "Medicines and mobile clinics for flood-hit villages.",
        description = "Waterborne diseases spread fast after floods. Mobile clinics bring doctors and medicines to families who cannot travel.",
        organizer = "Care Clinics",
        location = "Karachi, Pakistan",
        categoryId = SeedCategoryIds.health,
        imageUrl = "https://images.donnations.com/campaigns/medical-support.jpg",
        goal = Money(2_000_000, Currency.EUR),
        raised = Money(1_245_000, Currency.EUR),
        donorCount = 530,
        featured = false,
        createdAt = Instant.parse("2026-09-28T09:00:00Z"),
        endsAt = Instant.parse("2026-12-15T23:59:59Z"),
    ),
    Campaign(
        id = CampaignId(UUID.fromString("3f1c2a5e-8d4b-4f0a-9c6e-1a2b3c4d5e06")),
        title = "Winter Relief for Families",
        summary = "Blankets, heaters and warm meals before winter.",
        description = "Families displaced by the earthquake face a harsh winter in temporary shelters. Help us deliver warmth before temperatures drop.",
        organizer = "Warm Hearts",
        location = "Gaziantep, Turkey",
        categoryId = SeedCategoryIds.emergency,
        imageUrl = "https://images.donnations.com/campaigns/winter-relief.jpg",
        goal = Money(1_000_000, Currency.EUR),
        raised = Money(821_000, Currency.EUR),
        donorCount = 960,
        featured = true,
        createdAt = Instant.parse("2026-10-05T09:00:00Z"),
        endsAt = Instant.parse("2027-01-31T23:59:59Z"),
    ),
)

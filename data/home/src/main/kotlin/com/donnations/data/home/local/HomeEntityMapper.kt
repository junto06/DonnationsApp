package com.donnations.data.home.local

import com.donnations.core.model.Cached
import com.donnations.core.model.CampaignId
import com.donnations.data.campaignstore.CampaignEntity
import com.donnations.data.campaignstore.CampaignSummaryEntity
import com.donnations.data.home.remote.dto.CampaignDto
import com.donnations.data.home.remote.dto.HomeDto
import com.donnations.domain.home.model.Campaign
import com.donnations.domain.home.model.Category
import com.donnations.domain.home.model.Home
import java.time.Instant

class HomeRefresh(
    val home: StoredHome,
    val campaigns: List<CampaignSummaryEntity>
)

fun HomeDto.toRefresh(updatedAt: Instant): HomeRefresh {
    val featured = featured.distinctBy { it.id }
    val campaigns = campaigns.distinctBy { it.id }
    return HomeRefresh(
        home = StoredHome(
            snapshot = HomeSnapshotEntity(updatedAtEpochMillis = updatedAt.toEpochMilli()),
            categories = categories.mapIndexed { position, category ->
                CategoryEntity(
                    position = position,
                    id = category.id,
                    name = category.name
                )
            },
            entries = featured.toEntries(HomeSection.FEATURED) + campaigns.toEntries(HomeSection.CAMPAIGNS),
        ),
        campaigns = (featured + campaigns).distinctBy { it.id }
            .map { it.toSummaryEntity(updatedAt) },
    )
}

fun StoredHome.toDomain(campaigns: List<CampaignEntity>): Cached<Home> {
    val campaignsById = campaigns.associateBy { it.id }
    fun inSection(section: HomeSection) = entries.filter { it.section == section }
        .sortedBy { it.position }
        .mapNotNull { campaignsById[it.campaignId]?.toDomain() }
    return Cached(
        value = Home(
            categories = categories.map { Category(it.id, it.name) },
            featured = inSection(HomeSection.FEATURED),
            campaigns = inSection(HomeSection.CAMPAIGNS),
        ),
        updatedAt = Instant.ofEpochMilli(snapshot.updatedAtEpochMillis),
    )
}

private fun List<CampaignDto>.toEntries(section: HomeSection) =
    mapIndexed { position, campaign -> HomeEntryEntity(section, position, campaign.id) }

private fun CampaignDto.toSummaryEntity(updatedAt: Instant) = CampaignSummaryEntity(
    id = id,
    title = title,
    summary = summary,
    location = location,
    categoryId = categoryId,
    imageUrl = imageUrl,
    currency = currency,
    goal = goal.toBigDecimal().toPlainString(),
    raised = raised.toBigDecimal().toPlainString(),
    donorCount = donorCount,
    endsAtEpochMillis = Instant.parse(endsAt).toEpochMilli(),
    updatedAtEpochMillis = updatedAt.toEpochMilli(),
)

private fun CampaignEntity.toDomain() = Campaign(
    id = CampaignId(id),
    title = title,
    summary = summary,
    location = location,
    categoryId = categoryId,
    imageUrl = imageUrl,
    currencyCode = currency,
    goal = goal.toBigDecimal(),
    raised = raised.toBigDecimal(),
)

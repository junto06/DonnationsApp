package com.donnations.data.home.local

import com.donnations.core.model.Cached
import com.donnations.core.model.CampaignId
import com.donnations.domain.home.model.Campaign
import com.donnations.domain.home.model.Category
import com.donnations.domain.home.model.Home
import java.time.Instant

fun Home.toStored(updatedAt: Instant): StoredHome = StoredHome(
    snapshot = HomeSnapshotEntity(updatedAtEpochMillis = updatedAt.toEpochMilli()),
    categories = categories.mapIndexed { position, category -> CategoryEntity(position, category.id, category.name) },
    campaigns = featured.toEntities(HomeSection.FEATURED) + campaigns.toEntities(HomeSection.CAMPAIGNS),
)

fun StoredHome.toDomain(): Cached<Home> = Cached(
    value = Home(
        categories = categories.map { Category(it.id, it.name) },
        featured = campaigns.inSection(HomeSection.FEATURED),
        campaigns = campaigns.inSection(HomeSection.CAMPAIGNS),
    ),
    updatedAt = Instant.ofEpochMilli(snapshot.updatedAtEpochMillis),
)

private fun List<Campaign>.toEntities(section: HomeSection) = mapIndexed { position, campaign ->
    CampaignEntity(
        section = section,
        position = position,
        id = campaign.id.value,
        title = campaign.title,
        summary = campaign.summary,
        location = campaign.location,
        categoryId = campaign.categoryId,
        imageUrl = campaign.imageUrl,
        currency = campaign.currencyCode,
        goal = campaign.goal.toPlainString(),
        raised = campaign.raised.toPlainString(),
    )
}

private fun List<CampaignEntity>.inSection(section: HomeSection) = filter { it.section == section }
    .sortedBy { it.position }
    .map {
        Campaign(
            id = CampaignId(it.id),
            title = it.title,
            summary = it.summary,
            location = it.location,
            categoryId = it.categoryId,
            imageUrl = it.imageUrl,
            currencyCode = it.currency,
            goal = it.goal.toBigDecimal(),
            raised = it.raised.toBigDecimal(),
        )
    }

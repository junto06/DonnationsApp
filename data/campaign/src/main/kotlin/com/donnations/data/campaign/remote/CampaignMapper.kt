package com.donnations.data.campaign.remote

import com.donnations.core.model.Cached
import com.donnations.core.model.CampaignId
import com.donnations.data.campaign.remote.dto.CampaignDetailDto
import com.donnations.data.campaignstore.CampaignEntity
import com.donnations.domain.campaign.model.CampaignDetail
import java.time.Instant

fun CampaignDetailDto.toEntity(updatedAt: Instant): CampaignEntity = CampaignEntity(
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
    description = description,
    organizer = organizer,
    updatedAtEpochMillis = updatedAt.toEpochMilli(),
)

// A campaign stored from a list (e.g. home) has no description/organizer yet; they're empty until the detail arrives.
fun CampaignEntity.toDomain(): Cached<CampaignDetail> = Cached(
    value = CampaignDetail(
        id = CampaignId(id),
        title = title,
        summary = summary,
        description = description.orEmpty(),
        organizer = organizer.orEmpty(),
        location = location,
        imageUrl = imageUrl,
        currencyCode = currency,
        goal = goal.toBigDecimal(),
        raised = raised.toBigDecimal(),
        donorCount = donorCount,
        endsAt = Instant.ofEpochMilli(endsAtEpochMillis),
    ),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
)

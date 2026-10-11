package com.donnations.data.campaign.remote

import com.donnations.core.model.CampaignId
import com.donnations.data.campaign.remote.dto.CampaignDetailDto
import com.donnations.domain.campaign.model.CampaignDetail
import java.time.Instant

fun CampaignDetailDto.toDomain(): CampaignDetail = CampaignDetail(
    id = CampaignId(id),
    title = title,
    summary = summary,
    description = description,
    organizer = organizer,
    location = location,
    imageUrl = imageUrl,
    currencyCode = currency,
    goal = goal.toBigDecimal(),
    raised = raised.toBigDecimal(),
    donorCount = donorCount,
    endsAt = Instant.parse(endsAt),
)

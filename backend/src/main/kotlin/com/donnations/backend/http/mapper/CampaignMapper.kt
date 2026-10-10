package com.donnations.backend.http.mapper

import com.donnations.backend.domain.model.Campaign
import com.donnations.backend.domain.model.Money
import com.donnations.backend.http.dto.CampaignDetailDto
import com.donnations.backend.http.dto.CampaignSummaryDto

private fun Money.toDecimalString(): String = amount.toPlainString()

fun Campaign.toSummaryDto(): CampaignSummaryDto = CampaignSummaryDto(
    id = id.value,
    title = title,
    summary = summary,
    imageUrl = imageUrl,
    currency = goal.currency.code,
    goal = goal.toDecimalString(),
    raised = raised.toDecimalString(),
    donorCount = donorCount,
    endsAt = endsAt,
)

fun Campaign.toDetailDto(): CampaignDetailDto = CampaignDetailDto(
    id = id.value,
    title = title,
    summary = summary,
    description = description,
    organizer = organizer,
    imageUrl = imageUrl,
    currency = goal.currency.code,
    goal = goal.toDecimalString(),
    raised = raised.toDecimalString(),
    donorCount = donorCount,
    createdAt = createdAt,
    endsAt = endsAt,
)

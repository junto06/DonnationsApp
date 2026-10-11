package com.donnations.data.home.remote

import com.donnations.core.model.CampaignId
import com.donnations.data.home.remote.dto.CampaignDto
import com.donnations.data.home.remote.dto.CategoryDto
import com.donnations.data.home.remote.dto.HomeDto
import com.donnations.domain.home.model.Campaign
import com.donnations.domain.home.model.Category
import com.donnations.domain.home.model.Home

fun HomeDto.toDomain(): Home = Home(
    categories = categories.map { it.toDomain() },
    featured = featured.map { it.toDomain() },
    campaigns = campaigns.map { it.toDomain() },
)

fun CategoryDto.toDomain(): Category = Category(id = id, name = name)

fun CampaignDto.toDomain(): Campaign = Campaign(
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

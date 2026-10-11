package com.donnations.feature.home

import com.donnations.domain.home.model.Campaign
import com.donnations.domain.home.model.Category
import com.donnations.domain.home.model.Home
import java.math.BigDecimal

fun campaign(
    id: String = "c1",
    goal: String = "10000.00",
    raised: String = "8210.00",
) = Campaign(
    id = id,
    title = "Winter Relief",
    summary = "Warm meals",
    location = "Gaziantep, Turkey",
    categoryId = "emergency",
    imageUrl = "https://example.com/a.jpg",
    currencyCode = "EUR",
    goal = BigDecimal(goal),
    raised = BigDecimal(raised),
)

fun home() = Home(
    categories = listOf(Category("education", "Education")),
    featured = listOf(campaign(id = "f1")),
    campaigns = listOf(campaign(id = "c1")),
)

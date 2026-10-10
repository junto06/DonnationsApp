package com.donnations.backend.http.mapper

import com.donnations.backend.domain.model.Category
import com.donnations.backend.domain.model.Home
import com.donnations.backend.http.dto.CategoryDto
import com.donnations.backend.http.dto.HomeDto

fun Home.toDto(): HomeDto = HomeDto(
    categories = categories.map { it.toDto() },
    featured = featured.map { it.toSummaryDto() },
    campaigns = campaigns.map { it.toSummaryDto() },
)

fun Category.toDto(): CategoryDto = CategoryDto(id = id.value, name = name)

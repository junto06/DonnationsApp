package com.donnations.data.home.remote

import com.donnations.data.home.remote.dto.CampaignDto
import com.donnations.data.home.remote.dto.CategoryDto
import com.donnations.data.home.remote.dto.HomeDto
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class HomeMapperTest {

    private val dto = CampaignDto(
        id = "c1",
        title = "Winter Relief",
        summary = "Warm meals",
        location = "Gaziantep, Turkey",
        categoryId = "emergency",
        imageUrl = "https://example.com/a.jpg",
        currency = "EUR",
        goal = "10000.00",
        raised = "8210.05",
    )

    @Test
    fun `maps amounts to exact decimals`() {
        val campaign = dto.toDomain()

        assertEquals(BigDecimal("10000.00"), campaign.goal)
        assertEquals(BigDecimal("8210.05"), campaign.raised)
        assertEquals("EUR", campaign.currencyCode)
    }

    @Test
    fun `keeps sections and order`() {
        val home = HomeDto(
            categories = listOf(CategoryDto("education", "Education"), CategoryDto("health", "Health")),
            featured = listOf(dto),
            campaigns = listOf(dto.copy(id = "c2"), dto.copy(id = "c3")),
        ).toDomain()

        assertEquals(listOf("education", "health"), home.categories.map { it.id })
        assertEquals(listOf("c1"), home.featured.map { it.id })
        assertEquals(listOf("c2", "c3"), home.campaigns.map { it.id })
    }
}

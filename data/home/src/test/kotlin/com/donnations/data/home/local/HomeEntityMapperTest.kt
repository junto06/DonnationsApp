package com.donnations.data.home.local

import com.donnations.core.model.CampaignId
import com.donnations.domain.home.model.Campaign
import com.donnations.domain.home.model.Category
import com.donnations.domain.home.model.Home
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

class HomeEntityMapperTest {

    private fun campaign(id: String) = Campaign(
        id = CampaignId(id),
        title = "Winter Relief",
        summary = "Warm meals",
        location = "Gaziantep, Turkey",
        categoryId = "emergency",
        imageUrl = "https://example.com/a.jpg",
        currencyCode = "EUR",
        goal = BigDecimal("10000.00"),
        raised = BigDecimal("8210.05"),
    )

    @Test
    fun `round-trips sections, order, exact amounts and the update time`() {
        val home = Home(
            categories = listOf(Category("education", "Education"), Category("health", "Health")),
            featured = listOf(campaign("c1")),
            campaigns = listOf(campaign("c2"), campaign("c1")),
        )
        val updatedAt = Instant.parse("2026-10-12T09:30:00Z")

        val cached = home.toStored(updatedAt).toDomain()

        assertEquals(home, cached.value)
        assertEquals(updatedAt, cached.updatedAt)
    }
}

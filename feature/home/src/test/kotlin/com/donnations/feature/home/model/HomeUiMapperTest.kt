package com.donnations.feature.home.model

import com.donnations.feature.home.campaign
import com.donnations.feature.home.home
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class HomeUiMapperTest {

    private fun mapper(locale: Locale = Locale.US) = HomeUiMapper { locale }

    @Test
    fun `prepends All tab to categories`() {
        val ui = mapper().map(home())

        assertEquals(listOf("all", "education"), ui.categories.map { it.id })
        assertEquals("All", ui.categories.first().name)
    }

    @Test
    fun `formats amounts and progress for the locale`() {
        val ui = mapper().map(home().copy(campaigns = listOf(campaign(goal = "20000.00", raised = "12450.00"))))

        val row = ui.campaigns.single()
        assertEquals("€12,450", row.raised)
        assertEquals("of €20,000", row.goal)
        assertEquals("62%", row.percent)
        assertEquals(0.6225f, row.progress)
    }

    @Test
    fun `uses locale grouping and symbol placement`() {
        val row = mapper(Locale.GERMANY).map(home()).campaigns.single()

        assertEquals("8.210 €", row.raised)
        assertEquals("82 %", row.percent)
    }

    @Test
    fun `zero goal yields zero progress instead of failing`() {
        val row = mapper().map(home().copy(campaigns = listOf(campaign(goal = "0.00", raised = "0.00")))).campaigns.single()

        assertEquals(0f, row.progress)
        assertEquals("0%", row.percent)
    }
}

package com.donnations.feature.home.model

import com.donnations.feature.home.campaign
import com.donnations.feature.home.model.HomeUiMapper.Companion.ALL_CATEGORY_ID as ALL
import com.donnations.feature.home.home
import com.donnations.feature.home.testStrings
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class HomeUiMapperTest {

    private fun mapper(locale: Locale = Locale.US) = HomeUiMapper({ locale }, testStrings)

    @Test
    fun `prepends All tab to categories`() {
        val ui = mapper().map(home(), ALL)

        assertEquals(listOf("all", "education", "emergency"), ui.categories.map { it.id })
        assertEquals("All", ui.categories.first().name)
    }

    @Test
    fun `formats amounts and progress for the locale`() {
        val ui = mapper().map(home().copy(campaigns = listOf(campaign(goal = "20000.00", raised = "12450.00"))), ALL)

        val row = ui.campaigns.single()
        assertEquals("€12,450", row.raised)
        assertEquals("of €20,000", row.goal)
        assertEquals("62%", row.percent)
        assertEquals(0.6225f, row.progress)
    }

    @Test
    fun `uses locale grouping and symbol placement`() {
        val row = mapper(Locale.GERMANY).map(home(), ALL).campaigns.first()

        assertEquals("8.210 €", row.raised)
        assertEquals("82 %", row.percent)
    }

    @Test
    fun `zero goal yields zero progress instead of failing`() {
        val row = mapper().map(home().copy(campaigns = listOf(campaign(goal = "0.00", raised = "0.00"))), ALL).campaigns.single()

        assertEquals(0f, row.progress)
        assertEquals("0%", row.percent)
    }

    @Test
    fun `All keeps every campaign`() {
        val ui = mapper().map(home(), ALL)

        assertEquals(ALL, ui.selectedCategoryId)
        assertEquals(listOf("f1"), ui.featured.map { it.id })
        assertEquals(listOf("c1", "c2"), ui.campaigns.map { it.id })
    }

    @Test
    fun `filters featured and campaigns by selected category`() {
        val ui = mapper().map(home(), "education")

        assertEquals("education", ui.selectedCategoryId)
        assertEquals(emptyList<String>(), ui.featured.map { it.id })
        assertEquals(listOf("c2"), ui.campaigns.map { it.id })
    }

    @Test
    fun `unknown category falls back to All`() {
        val ui = mapper().map(home(), "gone")

        assertEquals(ALL, ui.selectedCategoryId)
        assertEquals(listOf("c1", "c2"), ui.campaigns.map { it.id })
    }
}

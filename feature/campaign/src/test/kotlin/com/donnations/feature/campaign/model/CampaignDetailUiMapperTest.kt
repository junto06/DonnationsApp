package com.donnations.feature.campaign.model

import com.donnations.feature.campaign.campaignDetail
import com.donnations.feature.campaign.testStrings
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class CampaignDetailUiMapperTest {

    private fun mapper(locale: Locale = Locale.US) = CampaignDetailUiMapper({ locale }, testStrings)

    @Test
    fun `formats amounts, progress and labels for the locale`() {
        val ui = mapper().map(campaignDetail())

        assertEquals("€12,450", ui.raised)
        assertEquals("of €20,000", ui.goal)
        assertEquals("62%", ui.percent)
        assertEquals(0.6225f, ui.progress)
        assertEquals("Organized by Care Clinics", ui.organizer)
        assertEquals("530 donors", ui.donorCount)
        assertEquals("Ends Dec 15, 2026", ui.endsAt)
    }

    @Test
    fun `uses singular for one donor`() {
        assertEquals("1 donor", mapper().map(campaignDetail(donorCount = 1)).donorCount)
    }

    @Test
    fun `uses locale grouping and symbol placement`() {
        val ui = mapper(Locale.GERMANY).map(campaignDetail())

        assertEquals("12.450 €", ui.raised)
        assertEquals("62 %", ui.percent)
    }

    @Test
    fun `zero goal yields zero progress instead of failing`() {
        val ui = mapper().map(campaignDetail(goal = "0.00", raised = "0.00"))

        assertEquals(0f, ui.progress)
        assertEquals("0%", ui.percent)
    }
}

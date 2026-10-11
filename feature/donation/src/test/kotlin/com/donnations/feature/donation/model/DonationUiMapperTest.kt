package com.donnations.feature.donation.model

import com.donnations.feature.donation.donationOptions
import com.donnations.feature.donation.testStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class DonationUiMapperTest {

    private fun mapper(locale: Locale = Locale.US) = DonationUiMapper({ locale }, testStrings)

    private fun errorFor(amount: String, locale: Locale = Locale.US) =
        mapper(locale).map(donationOptions(), amount).amountError

    @Test
    fun `formats presets, their input values and the currency symbol`() {
        val ui = mapper().map(donationOptions(), amount = "")

        assertEquals("Books for Every Child", ui.campaignTitle)
        assertEquals(listOf("€10", "€25", "€50", "€100"), ui.presets.map { it.label })
        assertEquals(listOf("10", "25", "50", "100"), ui.presets.map { it.inputValue })
        assertEquals("€", ui.currencySymbol)
        assertEquals(listOf(false, false, false, false), ui.presets.map { it.selected })
    }

    @Test
    fun `uses locale symbol placement`() {
        assertEquals("10 €", mapper(Locale.GERMANY).map(donationOptions(), amount = "").presets.first().label)
    }

    @Test
    fun `highlights the preset equal to the amount, however it was entered`() {
        listOf("25", "25.00", " 25.0 ").forEach { amount ->
            assertEquals(amount, listOf(false, true, false, false), mapper().map(donationOptions(), amount).presets.map { it.selected })
        }
        assertEquals(listOf(false, true, false, false), mapper(Locale.GERMANY).map(donationOptions(), "25,00").presets.map { it.selected })
        assertEquals(listOf(false, false, false, false), mapper().map(donationOptions(), "26").presets.map { it.selected })
    }

    @Test
    fun `accepts amounts in the locale's decimal format`() {
        listOf("5", "12.5", "12.50", "").forEach { assertNull(it, errorFor(it)) }
        listOf("5", "12,5", "12,50").forEach { assertNull(it, errorFor(it, Locale.GERMANY)) }
    }

    @Test
    fun `rejects grouping and the other locale's decimal separator instead of misreading them`() {
        listOf("1,000", "1,234.5", "12,5").forEach { assertEquals(it, "Enter a valid amount", errorFor(it)) }
        listOf("1.000", "12.5").forEach { assertEquals(it, "Enter a valid amount", errorFor(it, Locale.GERMANY)) }
    }

    @Test
    fun `rejects zero, negative, over-precise and non-numeric amounts`() {
        listOf("0", "-3", "1.005", "abc", "12abc", "1e3").forEach { assertEquals(it, "Enter a valid amount", errorFor(it)) }
    }
}

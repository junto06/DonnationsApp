package com.donnations.feature.donation.model

import com.donnations.feature.donation.R
import com.donnations.feature.donation.donation
import com.donnations.feature.donation.donationOptions
import com.donnations.feature.donation.testStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class DonationUiMapperTest {

    private fun mapper(locale: Locale = Locale.US) = DonationUiMapper({ locale }, testStrings)

    private fun errorFor(amount: String, locale: Locale = Locale.US) =
        mapper(locale).map(donationOptions(), amount, DonationSubmission.Idle).amountError

    @Test
    fun `formats presets, their input values and the currency symbol`() {
        val ui = mapper().map(donationOptions(), amount = "", DonationSubmission.Idle)

        assertEquals("Books for Every Child", ui.campaignTitle)
        assertEquals(listOf("€10", "€25", "€50", "€100"), ui.presets.map { it.label })
        assertEquals(listOf("10", "25", "50", "100"), ui.presets.map { it.inputValue })
        assertEquals("€", ui.currencySymbol)
        assertEquals(listOf(false, false, false, false), ui.presets.map { it.selected })
    }

    @Test
    fun `uses locale symbol placement`() {
        assertEquals("10 €", mapper(Locale.GERMANY).map(donationOptions(), amount = "", DonationSubmission.Idle).presets.first().label)
    }

    @Test
    fun `highlights the preset equal to the amount, however it was entered`() {
        listOf("25", "25.00", " 25.0 ").forEach { amount ->
            assertEquals(amount, listOf(false, true, false, false), mapper().map(donationOptions(), amount, DonationSubmission.Idle).presets.map { it.selected })
        }
        assertEquals(listOf(false, true, false, false), mapper(Locale.GERMANY).map(donationOptions(), "25,00", DonationSubmission.Idle).presets.map { it.selected })
        assertEquals(listOf(false, false, false, false), mapper().map(donationOptions(), "26", DonationSubmission.Idle).presets.map { it.selected })
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
    fun `exposes the parsed amount only when it is valid`() {
        assertEquals(BigDecimal("12.5"), mapper().map(donationOptions(), "12.5", DonationSubmission.Idle).amountToDonate)
        assertEquals(BigDecimal("12.5"), mapper(Locale.GERMANY).map(donationOptions(), "12,5", DonationSubmission.Idle).amountToDonate)
        listOf("", "0", "1,000").forEach { assertNull(it, mapper().map(donationOptions(), it, DonationSubmission.Idle).amountToDonate) }
    }

    @Test
    fun `reflects submitting and failed submissions`() {
        assertTrue(mapper().map(donationOptions(), "25", DonationSubmission.Submitting).isSubmitting)
        val failed = mapper().map(donationOptions(), "25", DonationSubmission.Failed(R.string.donation_error_submit))
        assertFalse(failed.isSubmitting)
        assertEquals("Couldn't confirm your donation.", failed.submitError)
    }

    @Test
    fun `thanks message shows cents only when the amount has them`() {
        assertEquals(
            "Thank you! Your €25 donation to Books for Every Child has been received.",
            mapper().mapThanks(donationOptions(), donation(amount = "25.00")),
        )
        assertEquals(
            "Thank you! Your €12.50 donation to Books for Every Child has been received.",
            mapper().mapThanks(donationOptions(), donation(amount = "12.5")),
        )
    }

    @Test
    fun `rejects zero, negative, over-precise and non-numeric amounts`() {
        listOf("0", "-3", "1.005", "abc", "12abc", "1e3").forEach { assertEquals(it, "Enter a valid amount", errorFor(it)) }
    }
}

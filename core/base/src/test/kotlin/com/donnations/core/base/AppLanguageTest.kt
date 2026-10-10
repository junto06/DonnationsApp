package com.donnations.core.base

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class AppLanguageTest {

    @Test
    fun `supported preference keeps its region`() {
        assertEquals(Locale.forLanguageTag("en-GB"), AppLanguage.resolve(listOf(Locale.forLanguageTag("en-GB"))))
    }

    @Test
    fun `later supported preference wins over unsupported first choice`() {
        val preferred = listOf(Locale.forLanguageTag("de-DE"), Locale.forLanguageTag("en-US"))
        assertEquals(Locale.forLanguageTag("en-US"), AppLanguage.resolve(preferred))
    }

    @Test
    fun `unsupported only falls back to English with the device region`() {
        assertEquals(Locale.forLanguageTag("en-DE"), AppLanguage.resolve(listOf(Locale.forLanguageTag("de-DE"))))
        assertEquals(Locale.forLanguageTag("en-PK"), AppLanguage.resolve(listOf(Locale.forLanguageTag("ur-PK"))))
    }

    @Test
    fun `empty preferences fall back to plain English`() {
        assertEquals(Locale.ENGLISH, AppLanguage.resolve(emptyList()))
    }
}

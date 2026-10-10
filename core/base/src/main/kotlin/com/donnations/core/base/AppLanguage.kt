package com.donnations.core.base

import java.util.Locale

enum class AppLanguage(val tag: String) {
    ENGLISH("en"),
    ;

    companion object {
        val default = ENGLISH

        // First supported preference wins and keeps its region;
        // otherwise the default language with the top preference's region.
        fun resolve(preferred: List<Locale>): Locale {
            preferred.forEach { locale ->
                val match = entries.firstOrNull { it.tag == locale.language }
                if (match != null) return Locale.Builder().setLanguage(match.tag).setRegion(locale.country).build()
            }
            val region = preferred.firstOrNull()?.country.orEmpty()
            return Locale.Builder().setLanguage(default.tag).setRegion(region).build()
        }
    }
}

package com.donnations.core.base

import java.util.Locale

// Resolved per call so runtime language changes apply.
fun interface LocaleHandler {
    fun currentLocale(): Locale
}

package com.donnations.backend.domain.model

import java.math.BigDecimal

// Stored in minor units (cents) to avoid floating-point rounding on money.
data class Money(val amountMinor: Long, val currency: Currency) {
    init {
        require(amountMinor >= 0) { "Money cannot be negative: $amountMinor" }
    }

    val amount: BigDecimal get() = BigDecimal.valueOf(amountMinor, currency.fractionDigits)

    companion object {
        // Null when the amount is negative, has more decimals than the currency allows, or overflows.
        fun ofMajorOrNull(amount: BigDecimal, currency: Currency): Money? = runCatching {
            Money(amount.setScale(currency.fractionDigits).unscaledValue().longValueExact(), currency)
        }.getOrNull()
    }
}

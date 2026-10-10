package com.donnations.backend.domain.model

import java.math.BigDecimal

// Stored in minor units (cents) to avoid floating-point rounding on money.
data class Money(val amountMinor: Long, val currency: Currency) {
    init {
        require(amountMinor >= 0) { "Money cannot be negative: $amountMinor" }
    }

    val amount: BigDecimal get() = BigDecimal.valueOf(amountMinor, currency.fractionDigits)
}

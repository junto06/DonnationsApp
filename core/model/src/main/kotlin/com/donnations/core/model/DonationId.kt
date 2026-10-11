package com.donnations.core.model

@JvmInline
value class DonationId(val value: String) {
    init {
        require(value.isNotBlank()) { "DonationId must not be blank" }
    }
}

package com.donnations.backend.domain.model

data class DonationOptions(
    val campaign: Campaign,
    val suggestedAmounts: List<Money>,
)

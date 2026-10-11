package com.donnations.backend.domain.model

// An ended campaign still returns options (acceptingDonations = false) so clients can render its ended state.
data class DonationOptions(
    val campaign: Campaign,
    val acceptingDonations: Boolean,
    val suggestedAmounts: List<Money>,
)

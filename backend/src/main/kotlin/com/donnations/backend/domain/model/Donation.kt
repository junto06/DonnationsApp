package com.donnations.backend.domain.model

import java.time.Instant
import java.util.UUID

@JvmInline
value class DonationId(val value: UUID)

data class Donation(
    val id: DonationId,
    val campaignId: CampaignId,
    val amount: Money,
    val createdAt: Instant,
) {
    init {
        require(amount.amountMinor > 0) { "Donation amount must be positive" }
    }
}

package com.donnations.domain.donation.usecase

import com.donnations.core.model.CampaignId
import com.donnations.core.model.DonationId
import com.donnations.domain.donation.exception.DonationStillProcessingException
import com.donnations.domain.donation.model.Donation
import com.donnations.domain.donation.model.DonationStatus
import com.donnations.domain.donation.repository.DonationRepository
import kotlinx.coroutines.delay
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class DonateUseCase @Inject constructor(
    private val repository: DonationRepository,
) {
    // The backend accepts the donation (202) and processes it asynchronously
    // so poll its status until it finishes.
    // Backoff keeps the usual case fast
    // the budget bounds how long the user waits before being told it is still going.
    suspend operator fun invoke(id: DonationId, campaignId: CampaignId, amount: BigDecimal): Donation {
        var donation = repository.submitDonation(id, campaignId, amount)
        var wait = FIRST_POLL_DELAY
        var remaining = POLL_BUDGET
        // Only COMPLETED or IN_PROGRESS arrive here
        // the repository throws for a failed donation.
        while (donation.status != DonationStatus.COMPLETED) {
            // Clamp the last pause so the whole wait never exceeds the budget.
            if (remaining <= Duration.ZERO) throw DonationStillProcessingException()
            val pause = minOf(wait, remaining)
            delay(pause)
            remaining -= pause
            wait = (wait * BACKOFF_FACTOR).coerceAtMost(MAX_POLL_DELAY)
            donation = repository.getDonation(id)
        }
        return donation
    }

    private companion object {
        val FIRST_POLL_DELAY = 300.milliseconds
        val MAX_POLL_DELAY = 2.seconds
        const val BACKOFF_FACTOR = 1.5
        val POLL_BUDGET = 10.seconds // total waiting before reporting "still processing"
    }
}

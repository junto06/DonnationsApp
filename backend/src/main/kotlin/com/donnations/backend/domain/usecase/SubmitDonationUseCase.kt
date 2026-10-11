package com.donnations.backend.domain.usecase

import com.donnations.backend.domain.exception.CampaignEndedException
import com.donnations.backend.domain.exception.CampaignNotFoundException
import com.donnations.backend.domain.exception.DonationConflictException
import com.donnations.backend.domain.exception.InvalidDonationAmountException
import com.donnations.backend.domain.model.CampaignId
import com.donnations.backend.domain.model.Donation
import com.donnations.backend.domain.model.DonationId
import com.donnations.backend.domain.model.Money
import com.donnations.backend.domain.queue.DonationQueue
import com.donnations.backend.domain.repository.CampaignsRepository
import com.donnations.backend.domain.repository.DonationsRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Clock

@Service
class SubmitDonationUseCase(
    private val campaignsRepository: CampaignsRepository,
    private val donationsRepository: DonationsRepository,
    private val donationQueue: DonationQueue,
    private val clock: Clock,
) {
    data class Result(val donation: Donation, val created: Boolean)

    // The client-chosen id makes retries safe: a repeat returns the original donation instead of charging twice.
    operator fun invoke(id: DonationId, campaignId: CampaignId, amount: BigDecimal): Result {
        // Replays are answered before validation so a retry still succeeds after the campaign ends.
        donationsRepository.getById(id)?.let { return replay(it, campaignId, amount) }

        val campaign = campaignsRepository.getById(campaignId) ?: throw CampaignNotFoundException()
        val now = clock.instant()
        if (!campaign.isOpenAt(now)) throw CampaignEndedException()
        val money = Money.ofMajorOrNull(amount, campaign.currency)?.takeIf { it.amountMinor > 0 }
            ?: throw InvalidDonationAmountException()

        val donation = Donation(id = id, campaignId = campaignId, amount = money, createdAt = now)
        if (!donationsRepository.create(donation)) {
            // Lost a race with a concurrent request carrying the same id.
            return replay(checkNotNull(donationsRepository.getById(id)), campaignId, amount)
        }
        donationQueue.enqueue(id)
        return Result(donation, created = true)
    }

    private fun replay(existing: Donation, campaignId: CampaignId, amount: BigDecimal): Result {
        val sameRequest = existing.campaignId == campaignId && existing.amount.amount.compareTo(amount) == 0
        if (!sameRequest) throw DonationConflictException()
        return Result(existing, created = false)
    }
}

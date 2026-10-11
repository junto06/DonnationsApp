package com.donnations.backend.domain.usecase

import com.donnations.backend.domain.exception.CampaignNotFoundException
import com.donnations.backend.domain.exception.DomainException
import com.donnations.backend.domain.model.DonationId
import com.donnations.backend.domain.model.DonationProcessingPolicy
import com.donnations.backend.domain.repository.CampaignsRepository
import com.donnations.backend.domain.repository.DonationsRepository
import org.springframework.stereotype.Service
import java.time.Clock

@Service
class ProcessDonationUseCase(
    private val campaignsRepository: CampaignsRepository,
    private val donationsRepository: DonationsRepository,
    private val policy: DonationProcessingPolicy,
    private val clock: Clock,
) {
    // Safe to run any number of times: the claim keeps workers apart, and applyDonation is idempotent per donation,
    // so a retry after a crash between the campaign update and the final save finishes without counting twice.
    // Unexpected exceptions propagate and leave the claim to expire; RecoverStuckDonationsUseCase re-enqueues it.
    operator fun invoke(id: DonationId) {
        val now = clock.instant()
        val donation = donationsRepository.claim(id, now, policy.lease) ?: return
        val processed = try {
            campaignsRepository.applyDonation(donation.campaignId, donation.id) { it.receive(donation.amount, now) }
                ?: throw CampaignNotFoundException()
            donation.complete(now)
        } catch (e: DomainException) {
            donation.fail(e.errorCode, now)
        }
        donationsRepository.save(processed)
    }
}

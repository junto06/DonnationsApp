package com.donnations.backend.domain.usecase

import com.donnations.backend.domain.exception.CampaignNotFoundException
import com.donnations.backend.domain.exception.InvalidDonationAmountException
import com.donnations.backend.domain.model.CampaignId
import com.donnations.backend.domain.model.Donation
import com.donnations.backend.domain.model.DonationId
import com.donnations.backend.domain.model.Money
import com.donnations.backend.domain.repository.CampaignsRepository
import com.donnations.backend.domain.repository.DonationsRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Clock
import java.util.UUID

@Service
class MakeDonationUseCase(
    private val campaignsRepository: CampaignsRepository,
    private val donationsRepository: DonationsRepository,
    private val clock: Clock,
) {
    operator fun invoke(campaignId: CampaignId, amount: BigDecimal): Donation {
        val campaign = campaignsRepository.getById(campaignId) ?: throw CampaignNotFoundException()
        // A campaign's currency never changes, so converting outside the atomic update is safe.
        val money = Money.ofMajorOrNull(amount, campaign.currency) ?: throw InvalidDonationAmountException()
        val now = clock.instant()
        // TODO(jpa): run update + save in one transaction (or insert the donation first);
        //  otherwise a failed save leaves the campaign counted with no donation record.
        campaignsRepository.update(campaignId) { it.receive(amount = money, at = now) } ?: throw CampaignNotFoundException()
        return donationsRepository.save(
            Donation(
                id = DonationId(UUID.randomUUID()),
                campaignId = campaignId,
                amount = money,
                createdAt = now
            ),
        )
    }
}

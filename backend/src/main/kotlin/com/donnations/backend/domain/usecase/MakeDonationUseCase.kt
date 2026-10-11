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
        val now = clock.instant()
        lateinit var donation: Donation
        campaignsRepository.update(campaignId) { campaign ->
            val money = Money.ofMajorOrNull(amount, campaign.currency) ?: throw InvalidDonationAmountException()
            campaign.receive(money, now).also {
                donation = Donation(DonationId(UUID.randomUUID()), campaignId, money, createdAt = now)
            }
        } ?: throw CampaignNotFoundException()
        return donationsRepository.save(donation)
    }
}

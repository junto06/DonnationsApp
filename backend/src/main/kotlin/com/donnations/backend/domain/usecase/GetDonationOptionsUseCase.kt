package com.donnations.backend.domain.usecase

import com.donnations.backend.domain.exception.CampaignEndedException
import com.donnations.backend.domain.exception.CampaignNotFoundException
import com.donnations.backend.domain.model.CampaignId
import com.donnations.backend.domain.model.DonationOptions
import com.donnations.backend.domain.model.Money
import com.donnations.backend.domain.repository.CampaignsRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Clock

@Service
class GetDonationOptionsUseCase(
    private val campaignsRepository: CampaignsRepository,
    private val clock: Clock,
) {
    operator fun invoke(campaignId: CampaignId): DonationOptions {
        val campaign = campaignsRepository.getById(campaignId) ?: throw CampaignNotFoundException()
        if (!campaign.isOpenAt(clock.instant())) throw CampaignEndedException()
        return DonationOptions(
            campaign = campaign,
            suggestedAmounts = SUGGESTED_MAJOR_AMOUNTS.map { checkNotNull(Money.ofMajorOrNull(BigDecimal(it), campaign.currency)) },
        )
    }

    private companion object {
        // Same presets for every campaign until amounts become configurable per market.
        val SUGGESTED_MAJOR_AMOUNTS = listOf(10, 25, 50, 100)
    }
}

package com.donnations.data.donation.remote

import com.donnations.core.model.CampaignId
import com.donnations.core.model.DonationId
import com.donnations.data.donation.remote.dto.DonationDto
import com.donnations.data.donation.remote.dto.DonationOptionsDto
import com.donnations.data.donation.remote.dto.DonationStatusDto
import com.donnations.domain.donation.model.Donation
import com.donnations.domain.donation.model.DonationOptions
import com.donnations.domain.donation.model.DonationStatus
import java.time.Instant

fun DonationOptionsDto.toDomain(): DonationOptions = DonationOptions(
    campaignId = CampaignId(campaignId),
    campaignTitle = campaignTitle,
    currencyCode = currency,
    acceptingDonations = acceptingDonations,
    suggestedAmounts = suggestedAmounts.map { it.toBigDecimal() },
)

// FAILED never reaches here: the repository turns it into the matching exception before mapping.
fun DonationDto.toDomain(): Donation = Donation(
    id = DonationId(id),
    campaignId = CampaignId(campaignId),
    amount = amount.toBigDecimal(),
    currencyCode = currency,
    createdAt = Instant.parse(createdAt),
    status = if (status == DonationStatusDto.COMPLETED) DonationStatus.COMPLETED else DonationStatus.IN_PROGRESS,
)

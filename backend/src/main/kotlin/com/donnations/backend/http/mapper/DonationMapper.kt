package com.donnations.backend.http.mapper

import com.donnations.backend.domain.exception.InvalidDonationAmountException
import com.donnations.backend.domain.model.Donation
import com.donnations.backend.domain.model.DonationOptions
import com.donnations.backend.http.dto.DonationDto
import com.donnations.backend.http.dto.DonationOptionsDto
import com.donnations.backend.http.dto.DonationRequestDto
import java.math.BigDecimal

fun DonationRequestDto.toAmount(): BigDecimal = amount.toBigDecimalOrNull() ?: throw InvalidDonationAmountException()

fun Donation.toDto(): DonationDto = DonationDto(
    id = id.value,
    campaignId = campaignId.value,
    currency = amount.currency.code,
    amount = amount.amount.toPlainString(),
    status = status.name,
    failureCode = failure?.value,
    createdAt = createdAt,
    processedAt = processedAt,
)

fun DonationOptions.toDto(): DonationOptionsDto = DonationOptionsDto(
    campaignId = campaign.id.value,
    campaignTitle = campaign.title,
    currency = campaign.currency.code,
    acceptingDonations = acceptingDonations,
    suggestedAmounts = suggestedAmounts.map { it.amount.toPlainString() },
)

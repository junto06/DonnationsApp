package com.donnations.feature.donation

import com.donnations.core.base.StringResolver
import com.donnations.core.model.CampaignId
import com.donnations.core.model.DonationId
import com.donnations.domain.donation.model.Donation
import com.donnations.domain.donation.model.DonationOptions
import com.donnations.domain.donation.model.DonationStatus
import java.math.BigDecimal
import java.time.Instant

fun donationOptions(campaignId: String = "c1", acceptingDonations: Boolean = true) = DonationOptions(
    campaignId = CampaignId(campaignId),
    campaignTitle = "Books for Every Child",
    currencyCode = "EUR",
    acceptingDonations = acceptingDonations,
    suggestedAmounts = listOf("10.00", "25.00", "50.00", "100.00").map(::BigDecimal),
)

fun donation(
    amount: String = "25.00",
    campaignId: String = "c1",
    status: DonationStatus = DonationStatus.COMPLETED,
) = Donation(
    id = DonationId("d1"),
    campaignId = CampaignId(campaignId),
    amount = BigDecimal(amount),
    currencyCode = "EUR",
    createdAt = Instant.parse("2026-10-11T10:00:00Z"),
    status = status,
)

// Mirrors res/values/strings.xml so mapper tests run on the JVM without Android resources.
val testStrings = object : StringResolver {
    override fun getString(id: Int, vararg formatArgs: Any): String = when (id) {
        R.string.donation_amount_invalid -> "Enter a valid amount"
        R.string.donation_thanks -> "Thank you! Your %1\$s donation to %2\$s has been received."
        R.string.donation_error_amount_rejected -> "This amount can't be accepted."
        R.string.donation_error_submit -> "Couldn't confirm your donation."
        R.string.donation_error_campaign_ended -> "This campaign has ended."
        R.string.donation_error_still_processing -> "Your donation is still being processed."
        else -> error("Unexpected string id $id")
    }.format(*formatArgs)

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): String =
        error("Unexpected plurals id $id")
}

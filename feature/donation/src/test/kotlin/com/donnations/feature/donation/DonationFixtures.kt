package com.donnations.feature.donation

import com.donnations.core.base.StringResolver
import com.donnations.core.model.CampaignId
import com.donnations.domain.donation.model.DonationOptions
import java.math.BigDecimal

fun donationOptions(campaignId: String = "c1") = DonationOptions(
    campaignId = CampaignId(campaignId),
    campaignTitle = "Books for Every Child",
    currencyCode = "EUR",
    suggestedAmounts = listOf("10.00", "25.00", "50.00", "100.00").map(::BigDecimal),
)

// Mirrors res/values/strings.xml so mapper tests run on the JVM without Android resources.
val testStrings = object : StringResolver {
    override fun getString(id: Int, vararg formatArgs: Any): String = when (id) {
        R.string.donation_amount_invalid -> "Enter a valid amount"
        else -> error("Unexpected string id $id")
    }

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): String =
        error("Unexpected plurals id $id")
}

package com.donnations.feature.campaign

import com.donnations.core.base.StringResolver
import com.donnations.domain.campaign.model.CampaignDetail
import java.math.BigDecimal
import java.time.Instant

fun campaignDetail(
    id: String = "c1",
    goal: String = "20000.00",
    raised: String = "12450.00",
    donorCount: Int = 530,
) = CampaignDetail(
    id = id,
    title = "Medical Support",
    summary = "Mobile clinics",
    description = "Doctors and medicines for flood-hit villages.",
    organizer = "Care Clinics",
    location = "Karachi, Pakistan",
    imageUrl = "https://example.com/a.jpg",
    currencyCode = "EUR",
    goal = BigDecimal(goal),
    raised = BigDecimal(raised),
    donorCount = donorCount,
    // Midday UTC so the formatted date is the same in any test machine's time zone.
    endsAt = Instant.parse("2026-12-15T12:00:00Z"),
)

// Mirrors res/values/strings.xml so mapper tests run on the JVM without Android resources.
val testStrings = object : StringResolver {
    override fun getString(id: Int, vararg formatArgs: Any): String = when (id) {
        R.string.campaign_detail_organized_by -> "Organized by %1\$s"
        R.string.campaign_detail_goal_of -> "of %1\$s"
        R.string.campaign_detail_ends -> "Ends %1\$s"
        else -> error("Unexpected string id $id")
    }.format(*formatArgs)

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): String = when (id) {
        R.plurals.campaign_detail_donors -> if (quantity == 1) "%1\$d donor" else "%1\$d donors"
        else -> error("Unexpected plurals id $id")
    }.format(*formatArgs)
}

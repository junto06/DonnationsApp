package com.donnations.feature.home

import com.donnations.core.model.CampaignId
import com.donnations.core.base.StringResolver
import com.donnations.domain.home.model.Campaign
import com.donnations.domain.home.model.Category
import com.donnations.domain.home.model.Home
import java.math.BigDecimal

fun campaign(
    id: String = "c1",
    categoryId: String = "emergency",
    goal: String = "10000.00",
    raised: String = "8210.00",
) = Campaign(
    id = CampaignId(id),
    title = "Winter Relief",
    summary = "Warm meals",
    location = "Gaziantep, Turkey",
    categoryId = categoryId,
    imageUrl = "https://example.com/a.jpg",
    currencyCode = "EUR",
    goal = BigDecimal(goal),
    raised = BigDecimal(raised),
)

fun home() = Home(
    categories = listOf(Category("education", "Education"), Category("emergency", "Emergency")),
    featured = listOf(campaign(id = "f1")),
    campaigns = listOf(campaign(id = "c1"), campaign(id = "c2", categoryId = "education")),
)

// Mirrors res/values/strings.xml so mapper tests run on the JVM without Android resources.
val testStrings = object : StringResolver {
    override fun getString(id: Int, vararg formatArgs: Any): String = when (id) {
        R.string.home_category_all -> "All"
        R.string.home_goal_of -> "of %1\$s".format(*formatArgs)
        else -> error("Unexpected string id $id")
    }

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): String =
        error("Unexpected plurals id $id")
}

package com.donnations.feature.home.model

import com.donnations.core.base.LocaleHandler
import com.donnations.domain.home.model.Campaign
import com.donnations.domain.home.model.Home
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

class HomeUiMapper @Inject constructor(
    private val localeHandler: LocaleHandler,
) {

    fun map(home: Home): HomeUiModel {
        val locale = localeHandler.currentLocale()
        return HomeUiModel(
            categories = listOf(CategoryUiModel(ALL_CATEGORY_ID, "All")) +
                home.categories.map { CategoryUiModel(it.id, it.name) },
            featured = home.featured.map { it.toUiModel(locale) },
            campaigns = home.campaigns.map { it.toUiModel(locale) },
        )
    }

    private fun Campaign.toUiModel(locale: Locale): CampaignUiModel {
        val money = NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(currencyCode)
            maximumFractionDigits = 0
        }
        val progress = if (goal.signum() == 0) BigDecimal.ZERO else raised.divide(goal, 4, RoundingMode.DOWN)
        return CampaignUiModel(
            id = id,
            title = title,
            summary = summary,
            location = location,
            imageUrl = imageUrl,
            raised = money.format(raised),
            goal = "of ${money.format(goal)}",
            percent = NumberFormat.getPercentInstance(locale).format(progress),
            progress = progress.toFloat(),
        )
    }

    companion object {
        const val ALL_CATEGORY_ID = "all"
    }
}

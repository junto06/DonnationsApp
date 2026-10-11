package com.donnations.feature.home.model

import com.donnations.core.base.LocaleHandler
import com.donnations.core.base.StringResolver
import com.donnations.domain.home.model.Campaign
import com.donnations.domain.home.model.Home
import com.donnations.feature.home.R
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

class HomeUiMapper @Inject constructor(
    private val localeHandler: LocaleHandler,
    private val strings: StringResolver,
) {

    fun map(home: Home, selectedCategoryId: String): HomeUiModel {
        val locale = localeHandler.currentLocale()
        // Falls back to All if the selected category disappears after a reload.
        val selected = selectedCategoryId.takeIf { id -> home.categories.any { it.id == id } } ?: ALL_CATEGORY_ID
        val inSelected = { campaign: Campaign -> selected == ALL_CATEGORY_ID || campaign.categoryId == selected }
        return HomeUiModel(
            categories = buildList {
                add(CategoryUiModel(ALL_CATEGORY_ID, strings.getString(R.string.home_category_all)))
                home.categories.mapTo(this) { CategoryUiModel(it.id, it.name) }
            },
            selectedCategoryId = selected,
            featured = home.featured.filter(inSelected).map { it.toUiModel(locale) },
            campaigns = home.campaigns.filter(inSelected).map { it.toUiModel(locale) },
        )
    }

    private fun Campaign.toUiModel(locale: Locale): CampaignUiModel {
        val money = NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(currencyCode)
            maximumFractionDigits = 0
        }
        val progress = if (goal.signum() == 0) BigDecimal.ZERO else raised.divide(goal, 4, RoundingMode.DOWN)
        return CampaignUiModel(
            id = id.value,
            title = title,
            summary = summary,
            location = location,
            imageUrl = imageUrl,
            raised = money.format(raised),
            goal = strings.getString(R.string.home_goal_of, money.format(goal)),
            percent = NumberFormat.getPercentInstance(locale).format(progress),
            progress = progress.toFloat(),
        )
    }

    companion object {
        const val ALL_CATEGORY_ID = "all"
    }
}

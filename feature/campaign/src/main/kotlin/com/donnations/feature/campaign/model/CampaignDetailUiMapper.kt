package com.donnations.feature.campaign.model

import com.donnations.core.base.LocaleHandler
import com.donnations.core.base.StringResolver
import com.donnations.domain.campaign.model.CampaignDetail
import com.donnations.feature.campaign.R
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Currency
import javax.inject.Inject

class CampaignDetailUiMapper @Inject constructor(
    private val localeHandler: LocaleHandler,
    private val strings: StringResolver,
) {
    fun map(campaign: CampaignDetail): CampaignDetailUiModel {
        val locale = localeHandler.currentLocale()
        val money = NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(campaign.currencyCode)
            maximumFractionDigits = 0
        }
        val progress = with(campaign) {
            if (goal.signum() == 0) BigDecimal.ZERO else raised.divide(goal, 4, RoundingMode.DOWN)
        }
        return CampaignDetailUiModel(
            title = campaign.title,
            summary = campaign.summary,
            description = campaign.description,
            organizer = strings.getString(R.string.campaign_detail_organized_by, campaign.organizer),
            location = campaign.location,
            imageUrl = campaign.imageUrl,
            raised = money.format(campaign.raised),
            goal = strings.getString(R.string.campaign_detail_goal_of, money.format(campaign.goal)),
            percent = NumberFormat.getPercentInstance(locale).format(progress),
            progress = progress.toFloat(),
            donorCount = strings.getQuantityString(R.plurals.campaign_detail_donors, campaign.donorCount, campaign.donorCount),
            endsAt = strings.getString(
                R.string.campaign_detail_ends,
                DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(campaign.endsAt.toEpochMilli()),
            ),
        )
    }
}

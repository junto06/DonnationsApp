package com.donnations.feature.donation.model

import com.donnations.core.base.LocaleHandler
import com.donnations.core.base.StringResolver
import com.donnations.domain.donation.model.Donation
import com.donnations.domain.donation.model.DonationOptions
import com.donnations.feature.donation.R
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

class DonationUiMapper @Inject constructor(
    private val localeHandler: LocaleHandler,
    private val strings: StringResolver,
) {

    fun map(options: DonationOptions, amount: String, submission: DonationSubmission): DonationUiModel {
        val locale = localeHandler.currentLocale()
        val currency = Currency.getInstance(options.currencyCode)
        val input = inputFormat(locale, currency)
        val parsed = parse(amount, input)
        val valid = isValid(parsed, currency)
        return DonationUiModel(
            campaignTitle = options.campaignTitle,
            presets = options.suggestedAmounts.map { preset ->
                AmountPresetUiModel(
                    label = formatMoney(preset, currency, locale),
                    inputValue = input.format(preset),
                    selected = parsed != null && parsed.compareTo(preset) == 0,
                )
            },
            currencySymbol = currency.getSymbol(locale),
            amount = amount,
            amountError = strings.getString(R.string.donation_amount_invalid).takeIf { amount.isNotBlank() && !valid },
            amountToDonate = parsed.takeIf { valid },
            isSubmitting = submission is DonationSubmission.Submitting,
            submitError = (submission as? DonationSubmission.Failed)?.let { strings.getString(it.message) },
        )
    }

    fun mapThanks(options: DonationOptions, donation: Donation): String {
        val locale = localeHandler.currentLocale()
        val amount = formatMoney(donation.amount, Currency.getInstance(donation.currencyCode), locale)
        return strings.getString(R.string.donation_thanks, amount, options.campaignTitle)
    }

    // Whole amounts without decimals ("€25"), others with the currency's full precision ("€12.50", never "€12.5").
    private fun formatMoney(amount: BigDecimal, currency: Currency, locale: Locale): String =
        NumberFormat.getCurrencyInstance(locale).apply {
            this.currency = currency
            val fraction = if (amount.stripTrailingZeros().scale() > 0) currency.defaultFractionDigits else 0
            minimumFractionDigits = fraction
            maximumFractionDigits = fraction
        }.format(amount)


    private fun inputFormat(locale: Locale, currency: Currency): DecimalFormat =
        (NumberFormat.getNumberInstance(locale) as DecimalFormat).apply {
            isParseBigDecimal = true
            isGroupingUsed = false
            minimumFractionDigits = 0
            maximumFractionDigits = currency.defaultFractionDigits
        }

    // Null unless the whole text is one number in the input format.
    private fun parse(text: String, format: DecimalFormat): BigDecimal? {
        val trimmed = text.trim()
        val position = ParsePosition(0)
        val number = format.parse(trimmed, position) as? BigDecimal
        return number.takeIf { trimmed.isNotEmpty() && position.index == trimmed.length }
    }

    private fun isValid(amount: BigDecimal?, currency: Currency): Boolean =
        amount != null && amount > BigDecimal.ZERO && amount.stripTrailingZeros().scale() <= currency.defaultFractionDigits
}

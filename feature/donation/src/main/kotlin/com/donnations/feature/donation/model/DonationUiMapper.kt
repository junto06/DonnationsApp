package com.donnations.feature.donation.model

import com.donnations.core.base.LocaleHandler
import com.donnations.core.base.StringResolver
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

    fun map(options: DonationOptions, amount: String): DonationUiModel {
        val locale = localeHandler.currentLocale()
        val currency = Currency.getInstance(options.currencyCode)
        val money = NumberFormat.getCurrencyInstance(locale).apply {
            this.currency = currency
            minimumFractionDigits = 0
            maximumFractionDigits = currency.defaultFractionDigits
        }
        val input = inputFormat(locale, currency)
        val parsed = parse(amount, input)
        return DonationUiModel(
            campaignTitle = options.campaignTitle,
            presets = options.suggestedAmounts.map { preset ->
                AmountPresetUiModel(
                    label = money.format(preset),
                    inputValue = input.format(preset),
                    selected = parsed != null && parsed.compareTo(preset) == 0,
                )
            },
            currencySymbol = currency.getSymbol(locale),
            amount = amount,
            amountError = strings.getString(R.string.donation_amount_invalid)
                .takeIf { amount.isNotBlank() && !isValid(parsed, currency) },
        )
    }


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

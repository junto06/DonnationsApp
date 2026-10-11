package com.donnations.feature.donation.model

import java.math.BigDecimal

data class DonationUiModel(
    val campaignTitle: String,
    val presets: List<AmountPresetUiModel>,
    val currencySymbol: String,
    val amount: String,
    val amountError: String?,
    val amountToDonate: BigDecimal?, // the parsed amount when valid, null disables Donate
    val isSubmitting: Boolean,
    val submitError: String?,
)

data class AmountPresetUiModel(
    val label: String,
    val inputValue: String,
    val selected: Boolean,
)

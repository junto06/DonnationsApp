package com.donnations.feature.donation.model

data class DonationUiModel(
    val campaignTitle: String,
    val presets: List<AmountPresetUiModel>,
    val currencySymbol: String,
    val amount: String,
    val amountError: String?,
)

data class AmountPresetUiModel(
    val label: String,
    val inputValue: String,
    val selected: Boolean,
)

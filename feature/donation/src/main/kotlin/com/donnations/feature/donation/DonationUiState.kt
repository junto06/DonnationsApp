package com.donnations.feature.donation

import androidx.annotation.StringRes
import com.donnations.feature.donation.model.DonationUiModel

sealed interface DonationUiState {
    data object Loading : DonationUiState
    data class Success(val donation: DonationUiModel) : DonationUiState
    data class Error(@StringRes val message: Int, val retryable: Boolean) : DonationUiState
    data class Donated(val message: String) : DonationUiState
    data class Ended(val campaignTitle: String) : DonationUiState
}

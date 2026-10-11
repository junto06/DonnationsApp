package com.donnations.feature.campaign

import androidx.annotation.StringRes
import com.donnations.feature.campaign.model.CampaignDetailUiModel

sealed interface CampaignDetailUiState {
    data object Loading : CampaignDetailUiState
    data class Success(val campaign: CampaignDetailUiModel) : CampaignDetailUiState
    data class Error(@StringRes val message: Int) : CampaignDetailUiState
}

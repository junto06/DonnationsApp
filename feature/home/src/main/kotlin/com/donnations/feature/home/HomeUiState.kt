package com.donnations.feature.home

import androidx.annotation.StringRes
import com.donnations.feature.home.model.HomeUiModel

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val home: HomeUiModel) : HomeUiState
    data class Error(@StringRes val message: Int) : HomeUiState
}

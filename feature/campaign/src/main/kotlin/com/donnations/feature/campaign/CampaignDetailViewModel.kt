package com.donnations.feature.campaign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donnations.domain.campaign.repository.CampaignRepository
import com.donnations.feature.campaign.model.CampaignDetailUiMapper
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel(assistedFactory = CampaignDetailViewModel.Factory::class)
class CampaignDetailViewModel @AssistedInject constructor(
    @Assisted campaignId: String,
    repository: CampaignRepository,
    mapper: CampaignDetailUiMapper,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(campaignId: String): CampaignDetailViewModel
    }

    private val loadRequests = MutableStateFlow(0)

    val uiState: StateFlow<CampaignDetailUiState> = loadRequests
        .flatMapLatest {
            repository.getCampaign(campaignId)
                .map<_, CampaignDetailUiState> { CampaignDetailUiState.Success(mapper.map(it)) }
                .onStart { emit(CampaignDetailUiState.Loading) }
                .catch { emit(CampaignDetailUiState.Error(R.string.campaign_detail_error_load)) }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            CampaignDetailUiState.Loading
        )

    fun retry() {
        loadRequests.update { it + 1 }
    }
}

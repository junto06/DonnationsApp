package com.donnations.feature.campaign

import androidx.lifecycle.ViewModel
import com.donnations.core.base.fold
import com.donnations.core.base.mapContent
import com.donnations.core.common.HasCoroutineExceptionHandler
import com.donnations.core.common.viewModelReportingScope
import com.donnations.core.model.CampaignId
import com.donnations.domain.campaign.repository.CampaignRepository
import com.donnations.feature.campaign.model.CampaignDetailUiMapper
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel(assistedFactory = CampaignDetailViewModel.Factory::class)
class CampaignDetailViewModel @AssistedInject constructor(
    // String, not CampaignId: Dagger can't handle value classes in injected signatures.
    @Assisted campaignId: String,
    repository: CampaignRepository,
    mapper: CampaignDetailUiMapper,
    override val exceptionHandler: CoroutineExceptionHandler,
) : ViewModel(), HasCoroutineExceptionHandler {

    @AssistedFactory
    interface Factory {
        fun create(campaignId: String): CampaignDetailViewModel
    }

    private val id = CampaignId(campaignId)
    private val loadRequests = MutableStateFlow(0)

    // Stored campaign first (also one stored by home), refreshed in the background;
    // the error screen only appears when the refresh fails with nothing stored.
    // The repository never throws and mapContent turns mapper failures into errors, so no catch.
    val uiState: StateFlow<CampaignDetailUiState> = loadRequests
        .flatMapLatest {
            repository.getCampaign(id)
                .mapContent { mapper.map(it.value) }
                .map { campaign ->
                    campaign.fold(
                        onLoading = { CampaignDetailUiState.Loading },
                        onError = { CampaignDetailUiState.Error(R.string.campaign_detail_error_load) },
                        onContent = { CampaignDetailUiState.Success(it) },
                    )
                }
        }
        .stateIn(
            viewModelReportingScope,
            SharingStarted.WhileSubscribed(5_000),
            CampaignDetailUiState.Loading
        )

    fun retry() {
        loadRequests.update { it + 1 }
    }
}

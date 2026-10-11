package com.donnations.feature.donation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donnations.core.model.CampaignId
import com.donnations.domain.donation.exception.CampaignEndedException
import com.donnations.domain.donation.exception.CampaignNotFoundException
import com.donnations.domain.donation.exception.DonationException
import com.donnations.domain.donation.repository.DonationRepository
import com.donnations.feature.donation.model.DonationUiMapper
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel(assistedFactory = DonationViewModel.Factory::class)
class DonationViewModel @AssistedInject constructor(
    @Assisted campaignId: String,
    repository: DonationRepository,
    mapper: DonationUiMapper,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(campaignId: String): DonationViewModel
    }

    private val loadRequests = MutableStateFlow(0)
    private val amount = MutableStateFlow("")

    val uiState: StateFlow<DonationUiState> = loadRequests
        .flatMapLatest {
            repository.getDonationOptions(CampaignId(campaignId))
                .combine<_, _, DonationUiState>(amount) { options, amount ->
                    DonationUiState.Success(mapper.map(options, amount))
                }
                .onStart { emit(DonationUiState.Loading) }
                .catch { emit(it.toErrorState()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DonationUiState.Loading)

    // Typing and tapping a preset both just set the amount.
    fun changeAmount(text: String) {
        amount.value = text
    }

    fun retry() {
        loadRequests.update { it + 1 }
    }

    private fun Throwable.toErrorState(): DonationUiState.Error = when (this) {
        is DonationException -> when (this) {
            is CampaignEndedException -> DonationUiState.Error(
                R.string.donation_error_campaign_ended,
                retryable = false
            )

            is CampaignNotFoundException -> DonationUiState.Error(
                R.string.donation_error_campaign_not_found,
                retryable = false
            )
        }

        else -> DonationUiState.Error(R.string.donation_error_load, retryable = true)
    }
}

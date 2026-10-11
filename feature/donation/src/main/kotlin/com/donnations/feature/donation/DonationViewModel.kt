package com.donnations.feature.donation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donnations.core.model.CampaignId
import com.donnations.domain.donation.exception.CampaignEndedException
import com.donnations.domain.donation.exception.CampaignNotFoundException
import com.donnations.domain.donation.exception.DonationException
import com.donnations.domain.donation.exception.InvalidDonationAmountException
import com.donnations.domain.donation.repository.DonationRepository
import com.donnations.feature.donation.model.DonationSubmission
import com.donnations.feature.donation.model.DonationUiMapper
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DonationViewModel.Factory::class)
class DonationViewModel @AssistedInject constructor(
    @Assisted campaignId: String,
    private val repository: DonationRepository,
    mapper: DonationUiMapper,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(campaignId: String): DonationViewModel
    }

    private val campaignId = CampaignId(campaignId)
    private val loadRequests = MutableStateFlow(0)
    private val amount = MutableStateFlow("")
    private val submission = MutableStateFlow<DonationSubmission>(DonationSubmission.Idle)

    val uiState: StateFlow<DonationUiState> = loadRequests
        .flatMapLatest {
            combine(repository.getDonationOptions(this.campaignId), amount, submission) { options, amount, submission ->
                when (submission) {
                    is DonationSubmission.Done -> DonationUiState.Donated(mapper.mapThanks(options, submission.donation))
                    else -> DonationUiState.Success(mapper.map(options, amount, submission))
                }
            }
                .onStart { emit(DonationUiState.Loading) }
                .catch { emit(it.toLoadErrorState()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DonationUiState.Loading)

    // Typing and tapping a preset both just set the amount; editing clears a previous submit error.
    fun changeAmount(text: String) {
        // Clear the error first so no frame shows the new amount next to the old error.
        if (submission.value is DonationSubmission.Failed) submission.value = DonationSubmission.Idle
        amount.value = text
    }

    fun donate() {
        val toDonate = (uiState.value as? DonationUiState.Success)?.donation?.amountToDonate ?: return
        if (submission.value is DonationSubmission.Submitting) return
        submission.value = DonationSubmission.Submitting
        viewModelScope.launch {
            submission.value = try {
                DonationSubmission.Done(repository.donate(campaignId, toDonate))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DonationSubmission.Failed(e.toSubmitErrorMessage())
            }
        }
    }

    fun retry() {
        loadRequests.update { it + 1 }
    }

    private fun Throwable.toLoadErrorState(): DonationUiState.Error = when (this) {
        is DonationException -> when (this) {
            is CampaignEndedException -> DonationUiState.Error(
                R.string.donation_error_campaign_ended,
                retryable = false
            )

            is CampaignNotFoundException -> DonationUiState.Error(
                R.string.donation_error_campaign_not_found,
                retryable = false
            )

            // Loading sends no amount, so the backend never rejects one here.
            is InvalidDonationAmountException -> DonationUiState.Error(R.string.donation_error_load, retryable = true)
        }

        else -> DonationUiState.Error(R.string.donation_error_load, retryable = true)
    }

    @StringRes
    private fun Throwable.toSubmitErrorMessage(): Int = when (this) {
        is DonationException -> when (this) {
            is InvalidDonationAmountException -> R.string.donation_error_amount_rejected
            is CampaignEndedException -> R.string.donation_error_campaign_ended
            is CampaignNotFoundException -> R.string.donation_error_campaign_not_found
        }

        else -> R.string.donation_error_submit
    }
}

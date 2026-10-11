package com.donnations.feature.donation

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donnations.core.model.CampaignId
import com.donnations.core.model.DonationId
import com.donnations.domain.donation.exception.CampaignEndedException
import com.donnations.domain.donation.exception.CampaignNotFoundException
import com.donnations.domain.donation.exception.DonationException
import com.donnations.domain.donation.exception.DonationStillProcessingException
import com.donnations.domain.donation.exception.InvalidDonationAmountException
import com.donnations.domain.donation.repository.DonationRepository
import com.donnations.domain.donation.usecase.DonateUseCase
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
import java.math.BigDecimal
import java.util.UUID

@HiltViewModel(assistedFactory = DonationViewModel.Factory::class)
class DonationViewModel @AssistedInject constructor(
    @Assisted campaignId: String,
    repository: DonationRepository,
    private val donateUseCase: DonateUseCase,
    mapper: DonationUiMapper,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(campaignId: String): DonationViewModel
    }

    private val campaignId = CampaignId(campaignId)
    private val loadRequests = MutableStateFlow(0)
    // Saved so the typed amount survives process death and a retry is one tap.
    private val amount = savedStateHandle.getStateFlow(AMOUNT_KEY, "")
    private val submission = MutableStateFlow<DonationSubmission>(DonationSubmission.Idle)

    val uiState: StateFlow<DonationUiState> = loadRequests
        .flatMapLatest {
            combine(repository.getDonationOptions(this.campaignId), amount, submission) { options, amount, submission ->
                when {
                    submission is DonationSubmission.Done ->
                        DonationUiState.Donated(mapper.mapThanks(options, submission.donation))
                    !options.acceptingDonations -> DonationUiState.Ended(options.campaignTitle)
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
        savedStateHandle[AMOUNT_KEY] = text
    }

    fun donate() {
        val toDonate = (uiState.value as? DonationUiState.Success)?.donation?.amountToDonate ?: return
        if (submission.value is DonationSubmission.Submitting) return
        val donationId = attemptIdFor(toDonate)
        submission.value = DonationSubmission.Submitting
        viewModelScope.launch {
            submission.value = try {
                DonationSubmission.Done(donateUseCase(donationId, campaignId, toDonate)).also { clearAttempts() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DonationSubmission.Failed(e.toSubmitErrorMessage())
            }
        }
    }

    // One idempotency key per amount among attempts not yet confirmed, so retrying an amount (even after editing
    // away and back, or after process death) reuses its key and can't double-charge. All cleared on success.
    private fun attemptIdFor(amount: BigDecimal): DonationId {
        val key = amount.stripTrailingZeros().toPlainString()
        val attempts = savedStateHandle.get<HashMap<String, String>>(ATTEMPTS_KEY) ?: HashMap()
        attempts[key]?.takeIf { it.isNotBlank() }?.let { return DonationId(it) }
        return DonationId(UUID.randomUUID().toString()).also {
            attempts[key] = it.value
            savedStateHandle[ATTEMPTS_KEY] = attempts
        }
    }

    private fun clearAttempts() {
        savedStateHandle.remove<HashMap<String, String>>(ATTEMPTS_KEY)
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

            // Loading sends no amount or donation, so these can't happen here.
            is InvalidDonationAmountException, is DonationStillProcessingException ->
                DonationUiState.Error(R.string.donation_error_load, retryable = true)
        }

        else -> DonationUiState.Error(R.string.donation_error_load, retryable = true)
    }

    @StringRes
    private fun Throwable.toSubmitErrorMessage(): Int = when (this) {
        is DonationException -> when (this) {
            is InvalidDonationAmountException -> R.string.donation_error_amount_rejected
            is CampaignEndedException -> R.string.donation_error_campaign_ended
            is CampaignNotFoundException -> R.string.donation_error_campaign_not_found
            is DonationStillProcessingException -> R.string.donation_error_still_processing
        }

        else -> R.string.donation_error_submit
    }

    private companion object {
        const val AMOUNT_KEY = "amount"
        const val ATTEMPTS_KEY = "attempts"
    }
}

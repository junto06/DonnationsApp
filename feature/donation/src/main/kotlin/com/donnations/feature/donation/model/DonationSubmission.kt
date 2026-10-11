package com.donnations.feature.donation.model

import androidx.annotation.StringRes
import com.donnations.domain.donation.model.Donation

sealed interface DonationSubmission {
    data object Idle : DonationSubmission
    data object Submitting : DonationSubmission
    data class Failed(@StringRes val message: Int) : DonationSubmission
    data class Done(val donation: Donation) : DonationSubmission
}

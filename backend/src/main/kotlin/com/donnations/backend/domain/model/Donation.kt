package com.donnations.backend.domain.model

import com.donnations.backend.domain.exception.ErrorCode
import java.time.Duration
import java.time.Instant
import java.util.UUID

@JvmInline
value class DonationId(val value: UUID)

enum class DonationStatus { PENDING, PROCESSING, COMPLETED, FAILED }

data class Donation(
    val id: DonationId,
    val campaignId: CampaignId,
    val amount: Money,
    val createdAt: Instant,
    val status: DonationStatus = DonationStatus.PENDING,
    val failure: ErrorCode? = null,
    val processedAt: Instant? = null,
    val claimedAt: Instant? = null,
) {
    init {
        require(amount.amountMinor > 0) { "Donation amount must be positive" }
        require((status == DonationStatus.FAILED) == (failure != null)) { "Only failed donations carry a failure" }
        require((status in UNPROCESSED) == (processedAt == null)) { "Processed donations need a time" }
        require(status != DonationStatus.PROCESSING || claimedAt != null) { "Processing donations need a claim time" }
    }

    // A worker that crashed mid-processing never finishes, so its claim expires after the lease and another can retry.
    fun isClaimableAt(time: Instant, lease: Duration): Boolean = when (status) {
        DonationStatus.PENDING -> true
        DonationStatus.PROCESSING -> !time.isBefore(claimedAt!!.plus(lease))
        DonationStatus.COMPLETED, DonationStatus.FAILED -> false
    }

    fun claim(at: Instant, lease: Duration): Donation {
        check(isClaimableAt(at, lease)) { "Donation is not claimable, status $status claimed at $claimedAt" }
        return copy(status = DonationStatus.PROCESSING, claimedAt = at)
    }

    fun complete(at: Instant): Donation {
        check(status == DonationStatus.PROCESSING) { "Only processing donations can complete, was $status" }
        return copy(status = DonationStatus.COMPLETED, processedAt = at)
    }

    fun fail(reason: ErrorCode, at: Instant): Donation {
        check(status == DonationStatus.PROCESSING) { "Only processing donations can fail, was $status" }
        return copy(status = DonationStatus.FAILED, failure = reason, processedAt = at)
    }

    private companion object {
        val UNPROCESSED = setOf(DonationStatus.PENDING, DonationStatus.PROCESSING)
    }
}

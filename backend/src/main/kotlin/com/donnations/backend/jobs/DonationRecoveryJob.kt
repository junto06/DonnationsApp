package com.donnations.backend.jobs

import com.donnations.backend.domain.usecase.RecoverStuckDonationsUseCase
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class DonationRecoveryJob(
    private val recoverStuckDonations: RecoverStuckDonationsUseCase,
) {
    private val log = LoggerFactory.getLogger(DonationRecoveryJob::class.java)

    @Scheduled(fixedDelayString = "\${donations.processing.recovery-interval}")
    fun run() {
        val recovered = recoverStuckDonations()
        if (recovered > 0) log.warn("Re-enqueued $recovered stuck donation(s)")
    }
}

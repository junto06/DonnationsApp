package com.donnations.backend.domain.usecase

import com.donnations.backend.domain.model.DonationProcessingPolicy
import com.donnations.backend.domain.queue.DonationQueue
import com.donnations.backend.domain.repository.DonationsRepository
import org.springframework.stereotype.Service
import java.time.Clock

@Service
class RecoverStuckDonationsUseCase(
    private val donationsRepository: DonationsRepository,
    private val donationQueue: DonationQueue,
    private val policy: DonationProcessingPolicy,
    private val clock: Clock,
) {
    // Re-enqueues donations idle for longer than the lease; duplicates are harmless because processing claims first.
    operator fun invoke(): Int {
        val stuck = donationsRepository.findStuck(olderThan = clock.instant().minus(policy.lease))
        stuck.forEach(donationQueue::enqueue)
        return stuck.size
    }
}

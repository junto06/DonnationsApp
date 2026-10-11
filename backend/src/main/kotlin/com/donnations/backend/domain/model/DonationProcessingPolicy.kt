package com.donnations.backend.domain.model

import java.time.Duration

// lease: how long a claimed donation stays with one worker before another may retry it.
data class DonationProcessingPolicy(val lease: Duration) {
    init {
        require(!lease.isNegative && !lease.isZero) { "Lease must be positive" }
    }
}

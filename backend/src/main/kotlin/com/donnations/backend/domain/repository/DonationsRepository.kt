package com.donnations.backend.domain.repository

import com.donnations.backend.domain.model.Donation
import com.donnations.backend.domain.model.DonationId
import java.time.Duration
import java.time.Instant

interface DonationsRepository {
    fun save(donation: Donation): Donation

    // Atomic insert-if-absent keyed by the client-chosen id; false when the id already exists.
    fun create(donation: Donation): Boolean

    fun getById(id: DonationId): Donation?

    // Atomically claims a PENDING donation, or a PROCESSING one whose lease expired; null if missing or held by a worker.
    fun claim(id: DonationId, at: Instant, lease: Duration): Donation?

    // PENDING donations created at or before olderThan (lost enqueue) and PROCESSING ones claimed at or before it (dead worker).
    fun findStuck(olderThan: Instant): List<DonationId>
}

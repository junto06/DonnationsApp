package com.donnations.backend.data.donations

import com.donnations.backend.domain.model.Donation
import java.time.Duration
import java.time.Instant
import java.util.UUID

interface DonationsStore {
    fun save(donation: Donation): Donation

    fun create(donation: Donation): Boolean

    fun get(id: UUID): Donation?

    fun claim(id: UUID, at: Instant, lease: Duration): Donation?

    fun findStuck(olderThan: Instant): List<Donation>
}

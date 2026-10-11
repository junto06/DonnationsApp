package com.donnations.backend.data.donations.store

import com.donnations.backend.data.donations.DonationsStore
import com.donnations.backend.domain.model.Donation
import com.donnations.backend.domain.model.DonationStatus
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

// Lost on restart, replace with a JPA-backed store
@Component
class InMemoryDonationsStore : DonationsStore {

    private val donations = ConcurrentHashMap<UUID, Donation>()

    override fun save(donation: Donation): Donation {
        donations[donation.id.value] = donation
        return donation
    }

    override fun create(donation: Donation): Boolean = donations.putIfAbsent(donation.id.value, donation) == null

    override fun get(id: UUID): Donation? = donations[id]

    // compute runs atomically per key: the in-memory equivalent of a conditional UPDATE on status and claimed_at.
    override fun claim(id: UUID, at: Instant, lease: Duration): Donation? {
        var claimed: Donation? = null
        donations.computeIfPresent(id) { _, donation ->
            if (donation.isClaimableAt(at, lease)) donation.claim(at, lease).also { claimed = it } else donation
        }
        return claimed
    }

    override fun findStuck(olderThan: Instant): List<Donation> = donations.values.filter {
        when (it.status) {
            DonationStatus.PENDING -> !it.createdAt.isAfter(olderThan)
            DonationStatus.PROCESSING -> !it.claimedAt!!.isAfter(olderThan)
            DonationStatus.COMPLETED, DonationStatus.FAILED -> false
        }
    }
}

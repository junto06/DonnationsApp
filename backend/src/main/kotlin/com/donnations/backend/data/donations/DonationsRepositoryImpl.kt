package com.donnations.backend.data.donations

import com.donnations.backend.domain.model.Donation
import com.donnations.backend.domain.model.DonationId
import com.donnations.backend.domain.repository.DonationsRepository
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.Instant

@Repository
class DonationsRepositoryImpl(
    private val donationsStore: DonationsStore,
) : DonationsRepository {
    override fun save(donation: Donation): Donation = donationsStore.save(donation)

    override fun create(donation: Donation): Boolean = donationsStore.create(donation)

    override fun getById(id: DonationId): Donation? = donationsStore.get(id.value)

    override fun claim(id: DonationId, at: Instant, lease: Duration): Donation? = donationsStore.claim(id.value, at, lease)

    override fun findStuck(olderThan: Instant): List<DonationId> = donationsStore.findStuck(olderThan).map { it.id }
}

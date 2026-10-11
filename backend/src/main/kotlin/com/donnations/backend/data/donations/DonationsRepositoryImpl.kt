package com.donnations.backend.data.donations

import com.donnations.backend.domain.model.Donation
import com.donnations.backend.domain.repository.DonationsRepository
import org.springframework.stereotype.Repository

@Repository
class DonationsRepositoryImpl(
    private val donationsStore: DonationsStore,
) : DonationsRepository {
    override fun save(donation: Donation): Donation = donationsStore.save(donation)
}

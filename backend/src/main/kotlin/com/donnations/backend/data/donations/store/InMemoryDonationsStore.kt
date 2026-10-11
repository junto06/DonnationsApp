package com.donnations.backend.data.donations.store

import com.donnations.backend.data.donations.DonationsStore
import com.donnations.backend.domain.model.Donation
import org.springframework.stereotype.Component
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
}

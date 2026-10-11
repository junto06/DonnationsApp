package com.donnations.backend.data.donations

import com.donnations.backend.domain.model.Donation

interface DonationsStore {
    fun save(donation: Donation): Donation
}

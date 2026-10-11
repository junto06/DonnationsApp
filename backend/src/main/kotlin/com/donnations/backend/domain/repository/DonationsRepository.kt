package com.donnations.backend.domain.repository

import com.donnations.backend.domain.model.Donation

interface DonationsRepository {
    fun save(donation: Donation): Donation
}

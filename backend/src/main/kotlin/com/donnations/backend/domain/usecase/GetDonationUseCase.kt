package com.donnations.backend.domain.usecase

import com.donnations.backend.domain.exception.DonationNotFoundException
import com.donnations.backend.domain.model.Donation
import com.donnations.backend.domain.model.DonationId
import com.donnations.backend.domain.repository.DonationsRepository
import org.springframework.stereotype.Service

@Service
class GetDonationUseCase(
    private val donationsRepository: DonationsRepository,
) {
    operator fun invoke(id: DonationId): Donation =
        donationsRepository.getById(id) ?: throw DonationNotFoundException()
}

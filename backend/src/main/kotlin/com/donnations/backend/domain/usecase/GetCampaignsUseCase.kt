package com.donnations.backend.domain.usecase

import com.donnations.backend.domain.model.Campaign
import com.donnations.backend.domain.repository.CampaignsRepository
import org.springframework.stereotype.Service

@Service
class GetCampaignsUseCase(
    private val campaignsRepository: CampaignsRepository,
) {
    operator fun invoke(): List<Campaign> =
        campaignsRepository.getAll().sortedByDescending { it.createdAt }
}

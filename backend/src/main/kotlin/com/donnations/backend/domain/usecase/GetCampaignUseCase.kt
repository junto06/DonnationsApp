package com.donnations.backend.domain.usecase

import com.donnations.backend.domain.exception.CampaignNotFoundException
import com.donnations.backend.domain.model.Campaign
import com.donnations.backend.domain.model.CampaignId
import com.donnations.backend.domain.repository.CampaignsRepository
import org.springframework.stereotype.Service

@Service
class GetCampaignUseCase(
    private val campaignsRepository: CampaignsRepository,
) {
    operator fun invoke(id: CampaignId): Campaign =
        campaignsRepository.getById(id) ?: throw CampaignNotFoundException()
}

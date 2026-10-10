package com.donnations.backend.domain.usecase

import com.donnations.backend.domain.model.Home
import com.donnations.backend.domain.repository.CampaignsRepository
import com.donnations.backend.domain.repository.CategoriesRepository
import org.springframework.stereotype.Service

@Service
class GetHomeUseCase(
    private val campaignsRepository: CampaignsRepository,
    private val categoriesRepository: CategoriesRepository,
) {
    // Featured campaigns go to the carousel and are left out of the list below it.
    operator fun invoke(): Home {
        val (featured, campaigns) = campaignsRepository.getAll()
            .sortedByDescending { it.createdAt }
            .partition { it.featured }
        return Home(
            categories = categoriesRepository.getAll(),
            featured = featured,
            campaigns = campaigns,
        )
    }
}

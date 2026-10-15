package com.donnations.data.campaign.remote

import com.donnations.data.campaign.remote.dto.CampaignDetailDto

// Shared by the repository and sync worker tests.
class FakeCampaignApi : CampaignApi {
    var failure: Throwable? = null
    var calls = 0
    var response = CampaignDetailDto(
        id = "c1",
        title = "Medical Support",
        summary = "Mobile clinics",
        description = "Doctors and medicines for flood-hit villages.",
        organizer = "Care Clinics",
        location = "Karachi, Pakistan",
        categoryId = "health",
        imageUrl = "https://example.com/a.jpg",
        currency = "EUR",
        goal = "20000.00",
        raised = "12450.00",
        donorCount = 530,
        endsAt = "2026-12-15T12:00:00Z",
    )

    override suspend fun getCampaign(id: String): CampaignDetailDto {
        calls++
        return failure?.let { throw it } ?: response
    }
}

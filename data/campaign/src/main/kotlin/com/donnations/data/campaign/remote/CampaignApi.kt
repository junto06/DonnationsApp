package com.donnations.data.campaign.remote

import com.donnations.data.campaign.remote.dto.CampaignDetailDto
import retrofit2.http.GET
import retrofit2.http.Path

interface CampaignApi {
    @GET("api/v1/campaigns/{id}")
    suspend fun getCampaign(@Path("id") id: String): CampaignDetailDto
}

package com.donnations.data.donation.remote

import com.donnations.data.donation.remote.dto.DonationOptionsDto
import retrofit2.http.GET
import retrofit2.http.Path

interface DonationApi {
    @GET("api/v1/campaigns/{id}/donation-options")
    suspend fun getDonationOptions(@Path("id") campaignId: String): DonationOptionsDto
}

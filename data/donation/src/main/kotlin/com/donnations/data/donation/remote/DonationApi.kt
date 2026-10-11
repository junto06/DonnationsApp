package com.donnations.data.donation.remote

import com.donnations.data.donation.remote.dto.DonationDto
import com.donnations.data.donation.remote.dto.DonationOptionsDto
import com.donnations.data.donation.remote.dto.DonationRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DonationApi {
    @GET("api/v1/campaigns/{id}/donation-options")
    suspend fun getDonationOptions(@Path("id") campaignId: String): DonationOptionsDto

    // 202 when created, 200 with the original on a retry of the same donationId.
    @POST("api/v1/campaigns/{id}/donations")
    suspend fun donate(@Path("id") campaignId: String, @Body request: DonationRequestDto): DonationDto

    @GET("api/v1/donations/{id}")
    suspend fun getDonation(@Path("id") donationId: String): DonationDto
}

package com.donnations.data.home.remote

import com.donnations.data.home.remote.dto.HomeDto
import retrofit2.http.GET

interface HomeApi {
    @GET("api/v1/home")
    suspend fun getHome(): HomeDto
}

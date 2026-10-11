package com.donnations.data.donation.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// The backend's error body for every endpoint; move to core:network:base once a second feature decodes it.
@Serializable
data class ErrorResponseDto(
    @SerialName("message") val message: String,
    @SerialName("errorCode") val errorCode: String,
)

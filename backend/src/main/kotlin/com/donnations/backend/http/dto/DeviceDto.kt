package com.donnations.backend.http.dto

data class DeviceRegistrationDto(
    val token: String, // FCM registration token from the app.
)

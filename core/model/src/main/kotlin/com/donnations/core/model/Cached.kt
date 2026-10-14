package com.donnations.core.model

import java.time.Instant

// A locally stored value and when it was last fetched from the backend.
data class Cached<out T>(
    val value: T,
    val updatedAt: Instant
)

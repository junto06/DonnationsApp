package com.donnations.backend.domain.model

import java.time.Instant

// FCM tokens are opaque, so only values that can't be a token are rejected.
@JvmInline
value class DeviceToken private constructor(val value: String) {
    companion object {
        private const val MAX_LENGTH = 4096

        fun ofOrNull(raw: String): DeviceToken? =
            raw.takeIf { it.isNotEmpty() && it.length <= MAX_LENGTH && it.none(Char::isWhitespace) }?.let(::DeviceToken)
    }
}

data class Device(
    val token: DeviceToken,
    // Refreshed on every registration, so stale tokens can be pruned later.
    val lastSeenAt: Instant,
)

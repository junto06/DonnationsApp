package com.donnations.backend.domain.notification

import com.donnations.backend.domain.model.DeviceToken

// Blocks on network I/O: call it from a worker (like the donation queue), not a request thread.
interface PushNotifier {
    fun send(message: PushMessage, devices: List<DeviceToken>): PushDelivery
}

// invalidTokens: devices the push service no longer knows (app uninstalled), safe to forget.
data class PushDelivery(
    val delivered: Int,
    val invalidTokens: Set<DeviceToken> = emptySet(),
)

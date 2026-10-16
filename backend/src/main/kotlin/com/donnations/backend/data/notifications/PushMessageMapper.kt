package com.donnations.backend.data.notifications

import com.donnations.backend.domain.notification.PushMessage
import com.donnations.backend.domain.notification.PushPriority
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.MulticastMessage

fun PushMessage.toMulticastMessage(tokens: List<String>): MulticastMessage =
    MulticastMessage.builder()
        .addAllTokens(tokens)
        .putAllData(toFcmData())
        .setAndroidConfig(AndroidConfig.builder().setPriority(priority.toAndroid()).build())
        .build()

// Split out because MulticastMessage hides its fields, so these are what tests can check.
// `type` is written last, so a data entry can't override it.
internal fun PushMessage.toFcmData(): Map<String, String> = data + (TYPE_KEY to type)

internal fun PushPriority.toAndroid(): AndroidConfig.Priority = when (this) {
    PushPriority.NORMAL -> AndroidConfig.Priority.NORMAL
    // Delivered promptly even while the app is in the background.
    PushPriority.HIGH -> AndroidConfig.Priority.HIGH
}

private const val TYPE_KEY = "type"

package com.donnations.backend.domain.notification

// Data-only: the system shows nothing, the app acts on `type` + `data`.
interface PushMessage {
    val type: String
    val data: Map<String, String>
    val priority: PushPriority get() = PushPriority.NORMAL
}

enum class PushPriority { NORMAL, HIGH }

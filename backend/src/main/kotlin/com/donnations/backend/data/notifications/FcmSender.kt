package com.donnations.backend.data.notifications

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MessagingErrorCode
import com.google.firebase.messaging.MulticastMessage

// Seam over FirebaseMessaging: its BatchResponse can't be built in tests, so FcmPushNotifier works on plain outcomes.
fun interface FcmSender {
    // One outcome per token, in order.
    fun send(message: MulticastMessage): List<FcmOutcome>
}

sealed interface FcmOutcome {
    data object Delivered : FcmOutcome

    data class Failed(val code: MessagingErrorCode?) : FcmOutcome
}

class FirebaseFcmSender(
    private val messaging: FirebaseMessaging,
) : FcmSender {
    override fun send(message: MulticastMessage): List<FcmOutcome> =
        messaging.sendEachForMulticast(message).responses.map { response ->
            response.exception?.let { FcmOutcome.Failed(it.messagingErrorCode) } ?: FcmOutcome.Delivered
        }
}

package com.donnations.backend.data.notifications

import com.donnations.backend.domain.model.DeviceToken
import com.donnations.backend.domain.notification.PushDelivery
import com.donnations.backend.domain.notification.PushMessage
import com.donnations.backend.domain.notification.PushNotifier
import com.google.firebase.messaging.MessagingErrorCode
import org.slf4j.LoggerFactory

class FcmPushNotifier(
    private val sender: FcmSender,
) : PushNotifier {
    private val log = LoggerFactory.getLogger(FcmPushNotifier::class.java)

    override fun send(message: PushMessage, devices: List<DeviceToken>): PushDelivery {
        var delivered = 0
        val invalidTokens = mutableSetOf<DeviceToken>()
        val transientFailures = mutableListOf<MessagingErrorCode?>()
        devices.chunked(MAX_TOKENS_PER_REQUEST).forEach { batch ->
            sender.send(message.toMulticastMessage(batch.map { it.value })).forEachIndexed { index, outcome ->
                when {
                    outcome is FcmOutcome.Delivered -> delivered++
                    outcome is FcmOutcome.Failed && outcome.code in PERMANENT_FAILURES -> invalidTokens += batch[index]
                    outcome is FcmOutcome.Failed -> transientFailures += outcome.code
                }
            }
        }
        if (transientFailures.isNotEmpty()) {
            log.warn(
                "Push {}: {} of {} devices failed {}",
                message.type, transientFailures.size, devices.size, transientFailures.groupingBy { it }.eachCount(),
            )
        }
        return PushDelivery(delivered, invalidTokens)
    }

    private companion object {
        const val MAX_TOKENS_PER_REQUEST = 500 // FCM's multicast limit

        // Tokens that will never work again. Not INVALID_ARGUMENT: a bad payload would drop every token.
        // Other failures (quota, unavailable) are transient.
        val PERMANENT_FAILURES = setOf(MessagingErrorCode.UNREGISTERED, MessagingErrorCode.SENDER_ID_MISMATCH)
    }
}

package com.donnations.backend.data.notifications

import com.donnations.backend.domain.model.DeviceToken
import com.donnations.backend.domain.notification.PushMessage
import com.google.firebase.messaging.MessagingErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals

class FcmPushNotifierTest {

    private val message = object : PushMessage {
        override val type = "TEST"
        override val data = emptyMap<String, String>()
    }

    private fun tokens(count: Int) = List(count) { DeviceToken.ofOrNull("t$it")!! }

    @Test
    fun `sends 1001 devices in three requests and counts deliveries`() {
        val batches = ArrayDeque(listOf(500, 500, 1))
        val notifier = FcmPushNotifier { _ -> List(batches.removeFirst()) { FcmOutcome.Delivered } }

        val delivery = notifier.send(message, tokens(1001))

        assertEquals(0, batches.size)
        assertEquals(1001, delivery.delivered)
        assertEquals(emptySet(), delivery.invalidTokens)
    }

    @Test
    fun `only permanently failed tokens are reported invalid`() {
        val devices = tokens(5)
        val notifier = FcmPushNotifier { _ ->
            listOf(
                FcmOutcome.Delivered,
                FcmOutcome.Failed(MessagingErrorCode.UNREGISTERED),
                FcmOutcome.Failed(MessagingErrorCode.SENDER_ID_MISMATCH),
                FcmOutcome.Failed(MessagingErrorCode.UNAVAILABLE),
                FcmOutcome.Failed(MessagingErrorCode.INVALID_ARGUMENT),
            )
        }

        val delivery = notifier.send(message, devices)

        assertEquals(1, delivery.delivered)
        assertEquals(setOf(devices[1], devices[2]), delivery.invalidTokens)
    }

    @Test
    fun `invalid tokens are matched to their position across batches`() {
        val devices = tokens(501)
        var call = 0
        val notifier = FcmPushNotifier { _ ->
            if (call++ == 0) List(500) { FcmOutcome.Delivered }
            else listOf(FcmOutcome.Failed(MessagingErrorCode.UNREGISTERED))
        }

        assertEquals(setOf(devices[500]), notifier.send(message, devices).invalidTokens)
    }

    @Test
    fun `no devices means no requests`() {
        val notifier = FcmPushNotifier { error("should not send") }

        assertEquals(0, notifier.send(message, emptyList()).delivered)
    }
}

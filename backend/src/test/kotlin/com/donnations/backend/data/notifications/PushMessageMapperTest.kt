package com.donnations.backend.data.notifications

import com.donnations.backend.domain.notification.PushMessage
import com.donnations.backend.domain.notification.PushPriority
import com.google.firebase.messaging.AndroidConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class PushMessageMapperTest {

    private val message = object : PushMessage {
        override val type = "TEST"
        override val data = mapOf("id" to "1", "type" to "spoofed")
    }

    @Test
    fun `data carries the payload plus the type, which can't be overridden`() {
        assertEquals(mapOf("id" to "1", "type" to "TEST"), message.toFcmData())
    }

    @Test
    fun `priorities map to Android priorities, normal by default`() {
        assertEquals(PushPriority.NORMAL, message.priority)
        assertEquals(AndroidConfig.Priority.NORMAL, PushPriority.NORMAL.toAndroid())
        assertEquals(AndroidConfig.Priority.HIGH, PushPriority.HIGH.toAndroid())
    }

    @Test
    fun `builds a multicast message for the tokens`() {
        message.toMulticastMessage(listOf("t1", "t2"))
    }
}

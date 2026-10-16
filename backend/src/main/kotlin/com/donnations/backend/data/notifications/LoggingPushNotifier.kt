package com.donnations.backend.data.notifications

import com.donnations.backend.domain.model.DeviceToken
import com.donnations.backend.domain.notification.PushDelivery
import com.donnations.backend.domain.notification.PushMessage
import com.donnations.backend.domain.notification.PushNotifier
import org.slf4j.LoggerFactory

class LoggingPushNotifier : PushNotifier {
    private val log = LoggerFactory.getLogger(LoggingPushNotifier::class.java)

    override fun send(message: PushMessage, devices: List<DeviceToken>): PushDelivery {
        log.info("Push disabled: {} {} to {} devices not sent", message.type, message.data, devices.size)
        return PushDelivery(delivered = 0)
    }
}

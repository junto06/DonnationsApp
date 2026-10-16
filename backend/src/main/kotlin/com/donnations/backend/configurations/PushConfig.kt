package com.donnations.backend.configurations

import com.donnations.backend.data.notifications.FcmPushNotifier
import com.donnations.backend.data.notifications.FirebaseFcmSender
import com.donnations.backend.data.notifications.LoggingPushNotifier
import com.donnations.backend.domain.notification.PushNotifier
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream

@Configuration
class PushConfig {
    // One bean read as a Boolean, so a typo such as `enabled: yes` fails startup instead of leaving no notifier.
    @Bean
    fun pushNotifier(
        @Value("\${donations.push.fcm.enabled:false}") enabled: Boolean,
        @Value("\${donations.push.fcm.credentials-file:}") credentialsFile: String,
    ): PushNotifier {
        if (!enabled) return LoggingPushNotifier()
        require(credentialsFile.isNotBlank()) { "donations.push.fcm.credentials-file is required when push is enabled" }
        val credentials = FileInputStream(credentialsFile).use(GoogleCredentials::fromStream)
        val app = FirebaseApp.initializeApp(FirebaseOptions.builder().setCredentials(credentials).build(), "donnations-push")
        return FcmPushNotifier(FirebaseFcmSender(FirebaseMessaging.getInstance(app)))
    }
}

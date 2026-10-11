package com.donnations.backend.configurations

import com.donnations.backend.domain.model.DonationProcessingPolicy
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import java.time.Duration

@Configuration
@EnableScheduling
class DonationProcessingConfig {
    @Bean
    fun donationProcessingPolicy(@Value("\${donations.processing.lease}") lease: Duration) =
        DonationProcessingPolicy(lease)
}

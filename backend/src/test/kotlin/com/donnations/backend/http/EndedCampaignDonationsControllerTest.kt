package com.donnations.backend.http

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

// Runs with the clock after every seeded campaign's end date (the nested @TestConfiguration is picked up automatically).
@SpringBootTest
@AutoConfigureMockMvc
class EndedCampaignDonationsControllerTest(@Autowired private val mockMvc: MockMvc) {

    @TestConfiguration
    class AfterAllCampaignsEnd {
        @Bean
        @Primary
        fun fixedClock(): Clock = Clock.fixed(Instant.parse("2030-01-01T00:00:00Z"), ZoneOffset.UTC)
    }

    @Test
    fun `options for an ended campaign return 200 with acceptingDonations false`() {
        mockMvc.get("/api/v1/campaigns/$BOOKS_ID/donation-options").andExpect {
            status { isOk() }
            jsonPath("$.campaignTitle") { value("Books for Every Child") }
            jsonPath("$.acceptingDonations") { value(false) }
            jsonPath("$.suggestedAmounts.length()") { value(0) }
        }
    }

    @Test
    fun `donating to an ended campaign returns 409`() {
        mockMvc.post("/api/v1/campaigns/$BOOKS_ID/donations") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"donationId":"${UUID.randomUUID()}","amount":"10.00"}"""
        }.andExpect {
            status { isConflict() }
            jsonPath("$.errorCode") { value("1006") }
        }
    }

    private companion object {
        const val BOOKS_ID = "3f1c2a5e-8d4b-4f0a-9c6e-1a2b3c4d5e02"
    }
}

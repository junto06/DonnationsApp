package com.donnations.backend.http

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class DonationsControllerTest(@Autowired private val mockMvc: MockMvc) {

    @Test
    fun `returns donation options`() {
        mockMvc.get("/api/v1/campaigns/$BOOKS_ID/donation-options").andExpect {
            status { isOk() }
            jsonPath("$.campaignTitle") { value("Books for Every Child") }
            jsonPath("$.currency") { value("EUR") }
            jsonPath("$.suggestedAmounts[0]") { value("10.00") }
            jsonPath("$.suggestedAmounts.length()") { value(4) }
        }
    }

    private companion object {
        const val BOOKS_ID = "3f1c2a5e-8d4b-4f0a-9c6e-1a2b3c4d5e02"
    }
}

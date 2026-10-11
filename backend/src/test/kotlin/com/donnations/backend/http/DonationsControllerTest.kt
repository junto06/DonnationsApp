package com.donnations.backend.http

import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.math.BigDecimal

// Donations mutate the seeded in-memory store, so later test classes get a fresh context.
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
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

    @Test
    fun `returns 404 for donation options of an unknown campaign`() {
        mockMvc.get("/api/v1/campaigns/00000000-0000-0000-0000-000000000000/donation-options").andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value("1004") }
        }
    }

    @Test
    fun `creates a donation and updates the campaign`() {
        val raisedBefore = raised()

        donate("""{"amount":"25.50"}""").andExpect {
            status { isCreated() }
            jsonPath("$.campaignId") { value(BOOKS_ID) }
            jsonPath("$.currency") { value("EUR") }
            jsonPath("$.amount") { value("25.50") }
            jsonPath("$.id") { exists() }
        }

        assertEquals(raisedBefore.add(BigDecimal("25.50")), raised())
    }

    @Test
    fun `rejects invalid amounts with error code`() {
        listOf("""{"amount":"0"}""", """{"amount":"1.005"}""", """{"amount":"abc"}""").forEach { body ->
            donate(body).andExpect {
                status { isBadRequest() }
                jsonPath("$.errorCode") { value("1005") }
            }
        }
    }

    @Test
    fun `rejects malformed body`() {
        donate("""{"nope":1}""").andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value("1002") }
        }
    }

    @Test
    fun `returns 404 for unknown campaign`() {
        mockMvc.post("/api/v1/campaigns/00000000-0000-0000-0000-000000000000/donations") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"amount":"10.00"}"""
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value("1004") }
        }
    }

    private fun raised(): BigDecimal {
        val body = mockMvc.get("/api/v1/campaigns/$BOOKS_ID").andReturn().response.contentAsString
        return BigDecimal(JsonPath.read<String>(body, "$.raised"))
    }

    private fun donate(body: String) = mockMvc.post("/api/v1/campaigns/$BOOKS_ID/donations") {
        contentType = MediaType.APPLICATION_JSON
        content = body
    }

    private companion object {
        const val BOOKS_ID = "3f1c2a5e-8d4b-4f0a-9c6e-1a2b3c4d5e02"
    }
}

package com.donnations.backend.http

import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.fail
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
import java.util.UUID

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
            jsonPath("$.acceptingDonations") { value(true) }
            jsonPath("$.suggestedAmounts[0]") { value("10.00") }
            jsonPath("$.suggestedAmounts.length()") { value(4) }
        }
    }

    @Test
    fun `accepts a donation, processes it async and updates the campaign`() {
        val raisedBefore = raised()

        val response = donate(amount = "25.50").andExpect {
            status { isAccepted() }
            jsonPath("$.status") { value("PENDING") }
            jsonPath("$.campaignId") { value(BOOKS_ID) }
            jsonPath("$.amount") { value("25.50") }
            jsonPath("$.failureCode") { value(null) }
        }.andReturn().response
        val location = response.getHeader("Location")!!
        assertTrue(location.startsWith("/api/v1/donations/"))

        awaitStatus(location, "COMPLETED")
        mockMvc.get(location).andExpect { jsonPath("$.processedAt") { exists() } }
        assertEquals(raisedBefore.add(BigDecimal("25.50")), raised())
    }

    @Test
    fun `retry with the same donationId returns 200 with the original donation`() {
        val donationId = UUID.randomUUID()
        donate(amount = "10.00", donationId = donationId).andExpect { status { isAccepted() } }

        donate(amount = "10.00", donationId = donationId).andExpect {
            status { isOk() }
            jsonPath("$.id") { value(donationId.toString()) }
            header { string("Location", "/api/v1/donations/$donationId") }
        }
    }

    @Test
    fun `reusing a donationId for a different amount returns 409`() {
        val donationId = UUID.randomUUID()
        donate(amount = "10.00", donationId = donationId).andExpect { status { isAccepted() } }

        donate(amount = "99.00", donationId = donationId).andExpect {
            status { isConflict() }
            jsonPath("$.errorCode") { value("1008") }
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
    fun `returns 404 for unknown donation`() {
        mockMvc.get("/api/v1/donations/00000000-0000-0000-0000-000000000000").andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value("1007") }
        }
    }

    @Test
    fun `rejects invalid amounts with error code`() {
        listOf("0", "1.005", "abc").forEach { amount ->
            donate(amount = amount).andExpect {
                status { isBadRequest() }
                jsonPath("$.errorCode") { value("1005") }
            }
        }
    }

    @Test
    fun `rejects body without donationId`() {
        donateRaw("""{"amount":"10.00"}""").andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value("1002") }
        }
    }

    @Test
    fun `returns 404 for unknown campaign`() {
        mockMvc.post("/api/v1/campaigns/00000000-0000-0000-0000-000000000000/donations") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"donationId":"${UUID.randomUUID()}","amount":"10.00"}"""
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value("1004") }
        }
    }

    private fun raised(): BigDecimal {
        val body = mockMvc.get("/api/v1/campaigns/$BOOKS_ID").andReturn().response.contentAsString
        return BigDecimal(JsonPath.read<String>(body, "$.raised"))
    }

    // The worker runs on its own thread, so poll the status endpoint like a client would.
    private fun awaitStatus(location: String, expected: String) {
        val deadline = System.currentTimeMillis() + 5_000
        while (System.currentTimeMillis() < deadline) {
            val body = mockMvc.get(location).andReturn().response.contentAsString
            if (body.contains("\"status\":\"$expected\"")) return
            Thread.sleep(20)
        }
        fail("Donation at $location never reached $expected")
    }

    private fun donate(amount: String, donationId: UUID = UUID.randomUUID()) =
        donateRaw("""{"donationId":"$donationId","amount":"$amount"}""")

    private fun donateRaw(body: String) = mockMvc.post("/api/v1/campaigns/$BOOKS_ID/donations") {
        contentType = MediaType.APPLICATION_JSON
        content = body
    }

    private companion object {
        const val BOOKS_ID = "3f1c2a5e-8d4b-4f0a-9c6e-1a2b3c4d5e02"
    }
}

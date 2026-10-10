package com.donnations.backend.http

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class CampaignsControllerTest(@Autowired private val mockMvc: MockMvc) {

    @Test
    fun `lists seeded campaigns newest first`() {
        mockMvc.get("/api/v1/campaigns").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(6) }
            jsonPath("$[0].title") { value("Winter Relief for Families") }
            jsonPath("$[0].currency") { value("EUR") }
            jsonPath("$[0].goal") { value("10000.00") }
            jsonPath("$[0].endsAt") { value("2027-01-31T23:59:59Z") }
            jsonPath("$[0].description") { doesNotExist() }
        }
    }

    @Test
    fun `returns campaign detail`() {
        mockMvc.get("/api/v1/campaigns/$CLEAN_WATER_ID").andExpect {
            status { isOk() }
            jsonPath("$.title") { value("Clean Water for Rural Schools") }
            jsonPath("$.organizer") { value("WaterBridge Foundation") }
            jsonPath("$.raised") { value("32750.00") }
        }
    }

    @Test
    fun `returns 404 with error code for unknown campaign`() {
        mockMvc.get("/api/v1/campaigns/00000000-0000-0000-0000-000000000000").andExpect {
            status { isNotFound() }
            jsonPath("$.message") { value("Campaign not found") }
            jsonPath("$.errorCode") { value("1004") }
        }
    }

    @Test
    fun `returns 400 with error code for malformed id`() {
        mockMvc.get("/api/v1/campaigns/not-a-uuid").andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value("1002") }
        }
    }

    @Test
    fun `returns 404 with error code for unknown route`() {
        mockMvc.get("/api/v1/nope").andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value("1003") }
        }
    }

    private companion object {
        const val CLEAN_WATER_ID = "3f1c2a5e-8d4b-4f0a-9c6e-1a2b3c4d5e01"
    }
}

package com.donnations.backend.http

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.UUID

// Registers devices in the in-memory store, so later test classes get a fresh context.
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class DevicesControllerTest(@Autowired private val mockMvc: MockMvc) {

    @Test
    fun `registers a device`() {
        registerDevice("token-${UUID.randomUUID()}").andExpect { status { isNoContent() } }
    }

    @Test
    fun `rejects a blank device token`() {
        registerDevice("").andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value("1009") }
        }
    }

    @Test
    fun `rejects a registration without a token`() {
        mockMvc.post("/api/v1/devices") {
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value("1002") }
        }
    }

    private fun registerDevice(token: String) = mockMvc.post("/api/v1/devices") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"token":"$token"}"""
    }
}

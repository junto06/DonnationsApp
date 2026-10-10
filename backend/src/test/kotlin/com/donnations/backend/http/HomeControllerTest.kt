package com.donnations.backend.http

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerTest(@Autowired private val mockMvc: MockMvc) {

    @Test
    fun `returns categories, featured carousel and remaining campaigns`() {
        mockMvc.get("/api/v1/home").andExpect {
            status { isOk() }
            jsonPath("$.categories.length()") { value(4) }
            jsonPath("$.categories[0].id") { value("education") }
            jsonPath("$.categories[0].name") { value("Education") }
            jsonPath("$.featured.length()") { value(3) }
            jsonPath("$.featured[0].title") { value("Winter Relief for Families") }
            jsonPath("$.featured[0].categoryId") { value("emergency") }
            jsonPath("$.featured[0].location") { value("Gaziantep, Turkey") }
            jsonPath("$.campaigns.length()") { value(3) }
            jsonPath("$.campaigns[0].title") { value("Medical Support for Flood Victims") }
            jsonPath("$.campaigns[0].raised") { value("12450.00") }
        }
    }
}

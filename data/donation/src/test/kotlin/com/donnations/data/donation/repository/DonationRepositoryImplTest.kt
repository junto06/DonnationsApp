package com.donnations.data.donation.repository

import com.donnations.core.model.CampaignId
import com.donnations.core.model.DonationId
import com.donnations.data.donation.remote.DonationApi
import com.donnations.data.donation.remote.dto.DonationDto
import com.donnations.data.donation.remote.dto.DonationOptionsDto
import com.donnations.data.donation.remote.dto.DonationRequestDto
import com.donnations.domain.donation.exception.CampaignEndedException
import com.donnations.domain.donation.exception.CampaignNotFoundException
import com.donnations.domain.donation.exception.InvalidDonationAmountException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.math.BigDecimal
import java.time.Instant

class DonationRepositoryImplTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val api = FakeApi()
    private val repository = DonationRepositoryImpl(api, json)

    @Test
    fun `maps options to exact decimals`() = runTest {
        val options = repository.getDonationOptions(CampaignId("c1")).first()

        assertEquals(CampaignId("c1"), options.campaignId)
        assertEquals("EUR", options.currencyCode)
        assertEquals(listOf(BigDecimal("10.00"), BigDecimal("25.00")), options.suggestedAmounts)
    }

    @Test
    fun `donating sends the plain decimal amount and maps the created donation`() = runTest {
        val donation = repository.donate(CampaignId("c1"), BigDecimal("12.5"))

        assertEquals(DonationRequestDto("12.5"), api.lastRequest)
        assertEquals(DonationId("d1"), donation.id)
        assertEquals(BigDecimal("12.50"), donation.amount)
        assertEquals(Instant.parse("2026-10-11T10:00:00Z"), donation.createdAt)
    }

    @Test
    fun `maps backend error codes to domain exceptions`() = runTest {
        assertTrue(donateFailure(404, "1004") is CampaignNotFoundException)
        assertTrue(donateFailure(400, "1005") is InvalidDonationAmountException)
        assertTrue(donateFailure(409, "1006") is CampaignEndedException)
        api.failure = httpError(409, "1006")
        assertTrue(runCatching { repository.getDonationOptions(CampaignId("c1")).first() }.exceptionOrNull() is CampaignEndedException)
    }

    @Test
    fun `other errors propagate unchanged, including bugs like a malformed request`() = runTest {
        assertEquals(400, (donateFailure(400, "1002") as HttpException).code())
        assertEquals(500, (donateFailure(500, body = "not json") as HttpException).code())
    }

    private suspend fun donateFailure(code: Int, errorCode: String = "", body: String? = null): Throwable {
        api.failure = httpError(code, errorCode, body)
        return runCatching { repository.donate(CampaignId("c1"), BigDecimal.TEN) }.exceptionOrNull()
            ?: error("Expected a failure for HTTP $code")
    }

    private fun httpError(code: Int, errorCode: String, body: String? = null) = HttpException(
        Response.error<Any>(code, (body ?: """{"message":"x","errorCode":"$errorCode"}""").toResponseBody()),
    )

    private class FakeApi : DonationApi {
        var failure: Throwable? = null
        var lastRequest: DonationRequestDto? = null

        override suspend fun getDonationOptions(campaignId: String): DonationOptionsDto {
            failure?.let { throw it }
            return DonationOptionsDto(campaignId, "Books for Every Child", "EUR", listOf("10.00", "25.00"))
        }

        override suspend fun donate(campaignId: String, request: DonationRequestDto): DonationDto {
            lastRequest = request
            failure?.let { throw it }
            return DonationDto("d1", campaignId, "EUR", "12.50", "2026-10-11T10:00:00Z")
        }
    }
}

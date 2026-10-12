package com.donnations.data.donation.repository

import com.donnations.core.base.RecordingLogger
import com.donnations.core.model.CampaignId
import com.donnations.core.model.DonationId
import com.donnations.data.donation.remote.DonationApi
import com.donnations.data.donation.remote.dto.DonationDto
import com.donnations.data.donation.remote.dto.DonationOptionsDto
import com.donnations.data.donation.remote.dto.DonationRequestDto
import com.donnations.data.donation.remote.dto.DonationStatusDto
import com.donnations.domain.donation.exception.CampaignEndedException
import com.donnations.domain.donation.exception.CampaignNotFoundException
import com.donnations.domain.donation.exception.InvalidDonationAmountException
import com.donnations.domain.donation.model.DonationStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.math.BigDecimal
import java.time.Instant

class DonationRepositoryImplTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val api = FakeApi()
    private val logger = RecordingLogger()
    private val repository = DonationRepositoryImpl(api, json, logger)
    private val id = DonationId("d1")

    @Test
    fun `maps options to exact decimals and the accepting flag`() = runTest {
        val options = repository.getDonationOptions(CampaignId("c1")).first()

        assertEquals(CampaignId("c1"), options.campaignId)
        assertEquals("EUR", options.currencyCode)
        assertTrue(options.acceptingDonations)
        assertEquals(listOf(BigDecimal("10.00"), BigDecimal("25.00")), options.suggestedAmounts)
    }

    @Test
    fun `submits the idempotency key and plain decimal, returning the pending donation without waiting`() = runTest {
        api.status = DonationStatusDto.PENDING

        val donation = repository.submitDonation(id, CampaignId("c1"), BigDecimal("12.5"))

        assertEquals(DonationRequestDto(donationId = "d1", amount = "12.5"), api.lastRequest)
        assertEquals(id, donation.id)
        assertEquals(BigDecimal("12.50"), donation.amount)
        assertEquals(DonationStatus.IN_PROGRESS, donation.status)
        assertEquals(Instant.parse("2026-10-11T10:00:00Z"), donation.createdAt)
    }

    @Test
    fun `a replay that is already completed comes back completed`() = runTest {
        api.status = DonationStatusDto.COMPLETED

        assertEquals(DonationStatus.COMPLETED, repository.submitDonation(id, CampaignId("c1"), BigDecimal.TEN).status)
    }

    @Test
    fun `gets the current status of a submitted donation`() = runTest {
        api.status = DonationStatusDto.PROCESSING

        val donation = repository.getDonation(id)

        assertEquals(id, donation.id)
        assertEquals(DonationStatus.IN_PROGRESS, donation.status)
    }

    @Test
    fun `a failed donation is thrown as the exception for its failure code`() = runTest {
        api.status = DonationStatusDto.FAILED
        api.failureCode = "1006"

        assertTrue(runCatching { repository.getDonation(id) }.exceptionOrNull() is CampaignEndedException)
        assertTrue(runCatching { repository.submitDonation(id, CampaignId("c1"), BigDecimal.TEN) }.exceptionOrNull() is CampaignEndedException)
    }

    @Test
    fun `an unmapped failure code is a bug and propagates`() = runTest {
        api.status = DonationStatusDto.FAILED
        api.failureCode = null

        assertTrue(runCatching { repository.getDonation(id) }.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `decodes an unseen status as UNKNOWN instead of failing to parse`() {
        val dto = json.decodeFromString<DonationDto>(
            """{"id":"d1","campaignId":"c1","currency":"EUR","amount":"10.00","status":"REFUNDED","createdAt":"2026-10-11T10:00:00Z"}""",
        )

        assertEquals(DonationStatusDto.UNKNOWN, dto.status)
    }

    @Test
    fun `an unknown status is treated as still in progress so status quo polling continues`() = runTest {
        api.status = DonationStatusDto.UNKNOWN

        assertEquals(DonationStatus.IN_PROGRESS, repository.getDonation(id).status)
    }

    @Test
    fun `maps backend error codes on submit and load to domain exceptions`() = runTest {
        assertTrue(failureFor(404, "1004") is CampaignNotFoundException)
        assertTrue(failureFor(400, "1005") is InvalidDonationAmountException)
        assertTrue(failureFor(409, "1006") is CampaignEndedException)
        api.httpFailure = httpError(404, "1004")
        assertTrue(runCatching { repository.getDonationOptions(CampaignId("c1")).first() }.exceptionOrNull() is CampaignNotFoundException)
    }

    @Test
    fun `other errors propagate unchanged, including bugs like a malformed request`() = runTest {
        assertEquals(400, (failureFor(400, "1002") as HttpException).code())
        assertEquals(500, (failureFor(500, body = "not json") as HttpException).code())
    }

    @Test
    fun `reports unmapped failures but not backend rejections or connectivity`() = runTest {
        failureFor(409, "1006")
        assertEquals(emptyList<Throwable>(), logger.recorded)

        failureFor(400, "1002")
        assertEquals(1, logger.recorded.size)

        logger.recorded.clear()
        api.httpFailure = IOException("offline")
        runCatching { repository.submitDonation(id, CampaignId("c1"), BigDecimal.TEN) }
        assertEquals(emptyList<Throwable>(), logger.recorded)
    }

    private suspend fun failureFor(code: Int, errorCode: String = "", body: String? = null): Throwable {
        api.httpFailure = httpError(code, errorCode, body)
        return runCatching { repository.submitDonation(id, CampaignId("c1"), BigDecimal.TEN) }.exceptionOrNull()
            ?: error("Expected submitDonation to fail for HTTP $code")
    }

    private fun httpError(code: Int, errorCode: String, body: String? = null) = HttpException(
        Response.error<Any>(code, (body ?: """{"message":"x","errorCode":"$errorCode"}""").toResponseBody()),
    )

    private class FakeApi : DonationApi {
        var httpFailure: Throwable? = null
        var status = DonationStatusDto.PENDING
        var failureCode: String? = null
        var lastRequest: DonationRequestDto? = null

        override suspend fun getDonationOptions(campaignId: String): DonationOptionsDto {
            httpFailure?.let { throw it }
            return DonationOptionsDto(campaignId, "Books for Every Child", "EUR", true, listOf("10.00", "25.00"))
        }

        override suspend fun donate(campaignId: String, request: DonationRequestDto): DonationDto {
            lastRequest = request
            httpFailure?.let { throw it }
            return dto(request.donationId, campaignId)
        }

        override suspend fun getDonation(donationId: String): DonationDto {
            httpFailure?.let { throw it }
            return dto(donationId, "c1")
        }

        private fun dto(id: String, campaignId: String): DonationDto {
            val failure = failureCode.takeIf { status == DonationStatusDto.FAILED }
            return DonationDto(id, campaignId, "EUR", "12.50", status, failure, "2026-10-11T10:00:00Z")
        }
    }
}

package com.donnations.data.donation.repository

import com.donnations.core.model.CampaignId
import com.donnations.data.donation.remote.DonationApi
import com.donnations.data.donation.remote.dto.DonationOptionsDto
import com.donnations.domain.donation.exception.CampaignEndedException
import com.donnations.domain.donation.exception.CampaignNotFoundException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.math.BigDecimal

class DonationRepositoryImplTest {

    private val dto = DonationOptionsDto(
        campaignId = "c1",
        campaignTitle = "Books for Every Child",
        currency = "EUR",
        suggestedAmounts = listOf("10.00", "25.00"),
    )

    @Test
    fun `maps options to exact decimals`() = runTest {
        val options = DonationRepositoryImpl(FakeApi { dto }).getDonationOptions(CampaignId("c1")).first()

        assertEquals(CampaignId("c1"), options.campaignId)
        assertEquals("EUR", options.currencyCode)
        assertEquals(listOf(BigDecimal("10.00"), BigDecimal("25.00")), options.suggestedAmounts)
    }

    @Test
    fun `maps 409 to campaign ended and 404 to not found`() = runTest {
        assertTrue(failureFor(409) is CampaignEndedException)
        assertTrue(failureFor(404) is CampaignNotFoundException)
    }

    @Test
    fun `other HTTP errors propagate unchanged`() = runTest {
        assertEquals(500, (failureFor(500) as HttpException).code())
    }

    private suspend fun failureFor(code: Int): Throwable {
        val api = FakeApi { throw HttpException(Response.error<Any>(code, "{}".toResponseBody())) }
        return runCatching { DonationRepositoryImpl(api).getDonationOptions(CampaignId("c1")).first() }
            .exceptionOrNull() ?: error("Expected a failure for HTTP $code")
    }

    private class FakeApi(private val respond: () -> DonationOptionsDto) : DonationApi {
        override suspend fun getDonationOptions(campaignId: String): DonationOptionsDto = respond()
    }
}

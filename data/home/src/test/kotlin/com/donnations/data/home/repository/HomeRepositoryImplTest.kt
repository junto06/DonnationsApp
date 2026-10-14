package com.donnations.data.home.repository

import com.donnations.core.base.RecordingLogger
import com.donnations.data.campaignstore.FakeCampaignDao
import com.donnations.data.home.local.FakeHomeDao
import com.donnations.data.home.remote.HomeApi
import com.donnations.data.home.remote.dto.CampaignDto
import com.donnations.data.home.remote.dto.CategoryDto
import com.donnations.data.home.remote.dto.HomeDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.IOException
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class HomeRepositoryImplTest {

    private val now = Instant.parse("2026-10-12T09:30:00Z")
    private val api = FakeApi()
    private val campaignDao = FakeCampaignDao()
    private val logger = RecordingLogger()
    private val repository = HomeRepositoryImpl(api, FakeHomeDao(), campaignDao, Clock.fixed(now, ZoneOffset.UTC), logger)

    @Test
    fun `nothing is stored before the first refresh`() = runTest {
        api.failure = IOException("offline")

        assertNull(load().content)
    }

    @Test
    fun `a refresh stores home with its sections, order, exact amounts and fetch time`() = runTest {
        api.response = api.response.copy(campaigns = listOf(campaignDto("c2"), campaignDto("c1")))

        load()

        val cached = stored()
        assertEquals(listOf("education"), cached.value.categories.map { it.id })
        assertEquals(listOf("c1"), cached.value.featured.map { it.id.value })
        assertEquals(listOf("c2", "c1"), cached.value.campaigns.map { it.id.value })
        assertEquals(BigDecimal("8210.05"), cached.value.featured.single().raised)
        assertEquals(now, cached.updatedAt)
    }

    @Test
    fun `a home without campaigns is stored and shown empty`() = runTest {
        api.response = api.response.copy(featured = emptyList(), campaigns = emptyList())

        load()

        val home = stored().value
        assertEquals(listOf("education"), home.categories.map { it.id })
        assertEquals(emptyList<Any>(), home.featured + home.campaigns)
    }

    @Test
    fun `a campaign listed twice in a section shows once`() = runTest {
        api.response = api.response.copy(campaigns = listOf(campaignDto("c2"), campaignDto("c2"), campaignDto("c3")))

        load()

        assertEquals(listOf("c2", "c3"), stored().value.campaigns.map { it.id.value })
    }

    @Test
    fun `stores campaigns in the shared store for other screens`() = runTest {
        load()

        val stored = campaignDao.rows.value.getValue("c1")
        assertEquals(960, stored.donorCount)
        assertEquals(Instant.parse("2027-01-31T23:59:59Z").toEpochMilli(), stored.endsAtEpochMillis)
        assertNull(stored.description)
    }

    @Test
    fun `a refresh keeps the description and organizer detail already stored`() = runTest {
        load()
        campaignDao.upsertCampaign(campaignDao.rows.value.getValue("c1").copy(description = "Long text", organizer = "Care"))
        api.response = api.response.copy(featured = listOf(campaignDto("c1").copy(raised = "9000.00")))

        load()

        val stored = campaignDao.rows.value.getValue("c1")
        assertEquals("9000.00", stored.raised)
        assertEquals("Long text", stored.description)
        assertEquals("Care", stored.organizer)
    }

    @Test
    fun `a failed refresh reports the error and keeps the stored copy`() = runTest {
        load()
        val offline = IOException("offline")
        api.failure = offline

        val settled = load()

        assertSame(offline, settled.error)
        assertEquals(listOf("c1"), stored().value.featured.map { it.id.value })
    }

    @Test
    fun `a malformed response is reported and never replaces the stored copy`() = runTest {
        load()
        api.response = api.response.copy(campaigns = listOf(campaignDto("c9").copy(goal = "not a number")))

        val failure = load().error

        assertEquals(listOf(failure), logger.recorded)
        assertEquals(listOf("c2"), stored().value.campaigns.map { it.id.value })
    }

    @Test
    fun `does not report connectivity failures`() = runTest {
        api.failure = IOException("offline")

        load()

        assertEquals(emptyList<Throwable>(), logger.recorded)
    }

    // Collects until the backend refresh settles (each collection fetches once).
    private suspend fun load() = repository.getHome().first { !it.loading }

    // What's stored now; the fetch this triggers is harmless in these tests.
    private suspend fun stored() = load().content!!

    private class FakeApi : HomeApi {
        var failure: Throwable? = null
        var response = HomeDto(
            categories = listOf(CategoryDto("education", "Education")),
            featured = listOf(campaignDto("c1")),
            campaigns = listOf(campaignDto("c2")),
        )

        override suspend fun getHome(): HomeDto = failure?.let { throw it } ?: response
    }
}

private fun campaignDto(id: String) = CampaignDto(
    id = id,
    title = "Winter Relief",
    summary = "Warm meals",
    location = "Gaziantep, Turkey",
    categoryId = "emergency",
    imageUrl = "https://example.com/a.jpg",
    currency = "EUR",
    goal = "10000.00",
    raised = "8210.05",
    donorCount = 960,
    endsAt = "2027-01-31T23:59:59Z",
)

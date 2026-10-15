package com.donnations.data.campaign.repository

import com.donnations.core.base.RecordingLogger
import com.donnations.core.model.CampaignId
import com.donnations.data.campaign.remote.FakeCampaignApi
import com.donnations.data.campaign.sync.FakeSyncScheduler
import com.donnations.data.campaignstore.CampaignSummaryEntity
import com.donnations.data.campaignstore.FakeCampaignDao
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

class CampaignRepositoryImplTest {

    private val now = Instant.parse("2026-10-12T09:30:00Z")
    private val id = CampaignId("c1")
    private val api = FakeCampaignApi()
    private val logger = RecordingLogger()
    private val dao = FakeCampaignDao()
    private val syncScheduler = FakeSyncScheduler()
    private val repository = CampaignRepositoryImpl(api, dao, Clock.fixed(now, ZoneOffset.UTC), syncScheduler, logger)

    @Test
    fun `nothing is stored before the first refresh`() = runTest {
        api.failure = IOException("offline")

        assertNull(load().content)
    }

    @Test
    fun `a refresh stores the campaign with the time it was fetched`() = runTest {
        load()

        val cached = stored()
        assertEquals(id, cached.value.id)
        assertEquals(BigDecimal("12450.00"), cached.value.raised)
        assertEquals(Instant.parse("2026-12-15T12:00:00Z"), cached.value.endsAt)
        assertEquals(now, cached.updatedAt)
    }

    @Test
    fun `a campaign stored by home shows with an empty description and organizer until its detail arrives`() = runTest {
        dao.upsertSummaries(listOf(summary()))
        api.failure = IOException("offline")

        val stored = load().content!!.value
        assertEquals("Medical Support", stored.title)
        assertEquals("", stored.description)
        assertEquals("", stored.organizer)

        api.failure = null

        assertEquals("Care Clinics", load().content!!.value.organizer)
    }

    @Test
    fun `a failed refresh reports the error and keeps the stored copy`() = runTest {
        load()
        val offline = IOException("offline")
        api.failure = offline

        val settled = load()

        assertSame(offline, settled.error)
        assertEquals(id, stored().value.id)
    }

    @Test
    fun `a malformed response is reported and never replaces the stored copy`() = runTest {
        load()
        api.response = api.response.copy(raised = "not a number")

        val failure = load().error

        assertEquals(listOf(failure), logger.recorded)
        assertEquals(BigDecimal("12450.00"), stored().value.raised)
    }

    @Test
    fun `an offline refresh schedules a background sync, also when a partial copy is stored`() = runTest {
        dao.upsertSummaries(listOf(summary()))
        api.failure = IOException("offline")

        val settled = load()

        assertEquals("Medical Support", settled.content!!.value.title)
        assertEquals(listOf(id), syncScheduler.scheduled)
    }

    @Test
    fun `a malformed response doesn't schedule a sync`() = runTest {
        api.response = api.response.copy(raised = "not a number")

        load()

        assertEquals(emptyList<CampaignId>(), syncScheduler.scheduled)
    }

    @Test
    fun `a successful refresh doesn't schedule a sync`() = runTest {
        load()

        assertEquals(emptyList<CampaignId>(), syncScheduler.scheduled)
    }

    @Test
    fun `does not report connectivity failures`() = runTest {
        api.failure = IOException("offline")

        load()

        assertEquals(emptyList<Throwable>(), logger.recorded)
    }

    // Collects until the backend refresh settles (each collection fetches once).
    private suspend fun load() = repository.getCampaign(id).first { !it.loading }

    // What's stored now; the fetch this triggers is harmless in these tests.
    private suspend fun stored() = load().content!!
}

private fun summary() = CampaignSummaryEntity(
    id = "c1",
    title = "Medical Support",
    summary = "Mobile clinics",
    location = "Karachi, Pakistan",
    categoryId = "health",
    imageUrl = "https://example.com/a.jpg",
    currency = "EUR",
    goal = "20000.00",
    raised = "12450.00",
    donorCount = 530,
    endsAtEpochMillis = Instant.parse("2026-12-15T12:00:00Z").toEpochMilli(),
    updatedAtEpochMillis = 0,
)

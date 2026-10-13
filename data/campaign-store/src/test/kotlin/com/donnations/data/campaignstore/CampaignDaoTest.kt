package com.donnations.data.campaignstore

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// Runs the real Room DAO, so FakeCampaignDao can't drift from it unnoticed.
@RunWith(RobolectricTestRunner::class)
class CampaignDaoTest {

    private val database = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext<Context>(),
        CampaignStoreDatabase::class.java,
    ).allowMainThreadQueries().build()
    private val dao = database.campaignDao()

    @After
    fun tearDown() = database.close()

    @Test
    fun `summaries insert new campaigns without description and organizer`() = runTest {
        dao.upsertSummaries(listOf(summary("c1")))

        val stored = dao.observeCampaign("c1").first()!!
        assertEquals("8210.05", stored.raised)
        assertNull(stored.description)
        assertNull(stored.organizer)
    }

    @Test
    fun `summaries update shared columns and keep a stored description and organizer`() = runTest {
        dao.upsertCampaign(detail("c1"))

        dao.upsertSummaries(listOf(summary("c1").copy(raised = "9000.00", updatedAtEpochMillis = 2)))

        val stored = dao.observeCampaign("c1").first()!!
        assertEquals("9000.00", stored.raised)
        assertEquals(2L, stored.updatedAtEpochMillis)
        assertEquals("Long text", stored.description)
        assertEquals("Care Clinics", stored.organizer)
    }

    @Test
    fun `a detail replaces the whole row`() = runTest {
        dao.upsertSummaries(listOf(summary("c1")))

        dao.upsertCampaign(detail("c1"))

        assertEquals("Care Clinics", dao.observeCampaign("c1").first()!!.organizer)
    }

    @Test
    fun `observes only the requested campaigns`() = runTest {
        dao.upsertSummaries(listOf(summary("c1"), summary("c2"), summary("c3")))

        assertEquals(setOf("c1", "c3"), dao.observeCampaigns(listOf("c1", "c3", "missing")).first().map { it.id }.toSet())
    }

    // SQLite (unlike standard SQL) accepts "IN ()" and matches nothing.
    @Test
    fun `an empty id list matches nothing`() = runTest {
        dao.upsertSummaries(listOf(summary("c1")))

        assertEquals(emptyList<CampaignEntity>(), dao.observeCampaigns(emptyList()).first())
    }

    private fun summary(id: String) = CampaignSummaryEntity(
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
        endsAtEpochMillis = 1_800_000_000_000,
        updatedAtEpochMillis = 1,
    )

    private fun detail(id: String) = CampaignEntity(
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
        endsAtEpochMillis = 1_800_000_000_000,
        description = "Long text",
        organizer = "Care Clinics",
        updatedAtEpochMillis = 1,
    )
}

package com.donnations.backend.domain.usecase

import com.donnations.backend.data.campaigns.CampaignsRepositoryImpl
import com.donnations.backend.data.campaigns.store.InMemoryCampaignsStore
import com.donnations.backend.domain.campaign
import com.donnations.backend.domain.exception.CampaignNotFoundException
import com.donnations.backend.domain.model.CampaignId
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CampaignUseCasesTest {

    private val older = campaign(title = "Older", createdAt = Instant.parse("2026-09-01T00:00:00Z"))
    private val newer = campaign(title = "Newer", createdAt = Instant.parse("2026-10-01T00:00:00Z"))
    private val repository = CampaignsRepositoryImpl(InMemoryCampaignsStore(listOf(older, newer)))

    @Test
    fun `lists campaigns newest first`() {
        assertEquals(listOf(newer, older), GetCampaignsUseCase(repository)())
    }

    @Test
    fun `gets campaign by id`() {
        assertEquals(older, GetCampaignUseCase(repository)(older.id))
    }

    @Test
    fun `throws when campaign is missing`() {
        assertFailsWith<CampaignNotFoundException> { GetCampaignUseCase(repository)(CampaignId(UUID.randomUUID())) }
    }
}

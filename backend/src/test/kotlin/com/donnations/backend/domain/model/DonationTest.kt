package com.donnations.backend.domain.model

import com.donnations.backend.domain.exception.ErrorCode
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DonationTest {

    private val created = Instant.parse("2026-11-01T10:00:00Z")
    private val processed = Instant.parse("2026-11-01T10:00:01Z")
    private val lease = Duration.ofSeconds(30)
    private val pending = Donation(
        id = DonationId(UUID.randomUUID()),
        campaignId = CampaignId(UUID.randomUUID()),
        amount = Money(1_000, Currency.EUR),
        createdAt = created,
    )

    @Test
    fun `moves pending to processing and then completed or failed`() {
        assertEquals(DonationStatus.PENDING, pending.status)
        val processing = pending.claim(created, lease)
        assertEquals(DonationStatus.PROCESSING, processing.status)
        assertEquals(DonationStatus.COMPLETED, processing.complete(processed).status)
        val failed = processing.fail(ErrorCode.CAMPAIGN_ENDED, processed)
        assertEquals(DonationStatus.FAILED, failed.status)
        assertEquals(ErrorCode.CAMPAIGN_ENDED, failed.failure)
        assertEquals(processed, failed.processedAt)
    }

    @Test
    fun `rejects transitions that skip or repeat a step`() {
        assertFailsWith<IllegalStateException> { pending.complete(processed) }
        assertFailsWith<IllegalStateException> { pending.fail(ErrorCode.CAMPAIGN_ENDED, processed) }
        assertFailsWith<IllegalStateException> { pending.claim(created, lease).claim(processed, lease) }
        assertFailsWith<IllegalStateException> { pending.claim(created, lease).complete(processed).complete(processed) }
    }

    @Test
    fun `a claim is exclusive until its lease expires`() {
        val claimed = pending.claim(created, lease)

        assertFalse(claimed.isClaimableAt(created.plus(lease).minusMillis(1), lease))
        assertTrue(claimed.isClaimableAt(created.plus(lease), lease))
        assertEquals(created.plus(lease), claimed.claim(created.plus(lease), lease).claimedAt)
        assertFalse(claimed.complete(processed).isClaimableAt(Instant.MAX, lease))
    }

    @Test
    fun `rejects inconsistent states`() {
        assertFailsWith<IllegalArgumentException> { pending.copy(amount = Money(0, Currency.EUR)) }
        assertFailsWith<IllegalArgumentException> { pending.copy(status = DonationStatus.FAILED) }
        assertFailsWith<IllegalArgumentException> { pending.copy(status = DonationStatus.COMPLETED) }
        assertFailsWith<IllegalArgumentException> { pending.copy(failure = ErrorCode.CAMPAIGN_ENDED) }
        assertFailsWith<IllegalArgumentException> { pending.copy(status = DonationStatus.PROCESSING) }
    }
}

package com.donnations.backend.domain.usecase

import com.donnations.backend.data.devices.DevicesRepositoryImpl
import com.donnations.backend.data.devices.store.InMemoryDevicesStore
import com.donnations.backend.domain.exception.InvalidDeviceTokenException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RegisterDeviceUseCaseTest {

    private val now = Instant.parse("2026-11-01T10:00:00Z")
    private val devices = DevicesRepositoryImpl(InMemoryDevicesStore())
    private val register = RegisterDeviceUseCase(devices, Clock.fixed(now, ZoneOffset.UTC))

    @Test
    fun `registering a token again keeps one device and refreshes it`() {
        RegisterDeviceUseCase(devices, Clock.fixed(now.minusSeconds(60), ZoneOffset.UTC))("token-a")
        register("token-a")

        assertEquals(listOf("token-a"), devices.getAll().map { it.token.value })
        assertEquals(now, devices.getAll().single().lastSeenAt)
    }

    @Test
    fun `a token of the maximum length is accepted`() {
        register("x".repeat(4096))

        assertEquals(1, devices.getAll().size)
    }

    @Test
    fun `blank, whitespace or oversized tokens are rejected`() {
        assertFailsWith<InvalidDeviceTokenException> { register("") }
        assertFailsWith<InvalidDeviceTokenException> { register("token with spaces") }
        assertFailsWith<InvalidDeviceTokenException> { register("x".repeat(4097)) }
    }
}

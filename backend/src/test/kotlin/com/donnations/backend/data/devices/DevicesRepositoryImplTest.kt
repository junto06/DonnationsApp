package com.donnations.backend.data.devices

import com.donnations.backend.data.devices.store.InMemoryDevicesStore
import com.donnations.backend.domain.model.Device
import com.donnations.backend.domain.model.DeviceToken
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class DevicesRepositoryImplTest {

    private val devices = DevicesRepositoryImpl(InMemoryDevicesStore())

    private fun device(token: String) = Device(DeviceToken.ofOrNull(token)!!, Instant.EPOCH)

    @Test
    fun `remove forgets only the given tokens and ignores unknown ones`() {
        listOf("a", "b", "c").forEach { devices.register(device(it)) }

        devices.remove(listOf(device("a").token, device("unknown").token))

        assertEquals(setOf("b", "c"), devices.getAll().map { it.token.value }.toSet())
    }
}

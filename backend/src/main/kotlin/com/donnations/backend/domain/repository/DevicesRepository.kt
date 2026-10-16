package com.donnations.backend.domain.repository

import com.donnations.backend.domain.model.Device
import com.donnations.backend.domain.model.DeviceToken

interface DevicesRepository {
    // Upsert: registering a known token refreshes it.
    fun register(device: Device)

    fun getAll(): List<Device>

    fun remove(tokens: Collection<DeviceToken>)
}

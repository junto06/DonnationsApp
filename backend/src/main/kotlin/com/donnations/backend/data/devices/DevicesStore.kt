package com.donnations.backend.data.devices

import com.donnations.backend.domain.model.Device

interface DevicesStore {
    fun save(device: Device)

    fun getAll(): List<Device>

    fun remove(tokens: Collection<String>)
}

package com.donnations.backend.data.devices.store

import com.donnations.backend.data.devices.DevicesStore
import com.donnations.backend.domain.model.Device
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryDevicesStore : DevicesStore {
    private val devices = ConcurrentHashMap<String, Device>()

    override fun save(device: Device) {
        devices[device.token.value] = device
    }

    override fun getAll(): List<Device> = devices.values.toList()

    override fun remove(tokens: Collection<String>) {
        tokens.forEach(devices::remove)
    }
}

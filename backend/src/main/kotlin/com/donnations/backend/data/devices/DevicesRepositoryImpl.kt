package com.donnations.backend.data.devices

import com.donnations.backend.domain.model.Device
import com.donnations.backend.domain.model.DeviceToken
import com.donnations.backend.domain.repository.DevicesRepository
import org.springframework.stereotype.Repository

@Repository
class DevicesRepositoryImpl(
    private val devicesStore: DevicesStore,
) : DevicesRepository {
    override fun register(device: Device) = devicesStore.save(device)

    override fun getAll(): List<Device> = devicesStore.getAll()

    override fun remove(tokens: Collection<DeviceToken>) = devicesStore.remove(tokens.map { it.value })
}

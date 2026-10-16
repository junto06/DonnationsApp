package com.donnations.backend.domain.usecase

import com.donnations.backend.domain.exception.InvalidDeviceTokenException
import com.donnations.backend.domain.model.Device
import com.donnations.backend.domain.model.DeviceToken
import com.donnations.backend.domain.repository.DevicesRepository
import org.springframework.stereotype.Service
import java.time.Clock

@Service
class RegisterDeviceUseCase(
    private val devicesRepository: DevicesRepository,
    private val clock: Clock,
) {
    operator fun invoke(token: String) {
        val deviceToken = DeviceToken.ofOrNull(token) ?: throw InvalidDeviceTokenException()
        devicesRepository.register(Device(deviceToken, lastSeenAt = clock.instant()))
    }
}

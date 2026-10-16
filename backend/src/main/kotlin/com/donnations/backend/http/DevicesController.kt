package com.donnations.backend.http

import com.donnations.backend.domain.usecase.RegisterDeviceUseCase
import com.donnations.backend.http.dto.DeviceRegistrationDto
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/devices")
class DevicesController(
    private val registerDeviceUseCase: RegisterDeviceUseCase,
) {
    @PostMapping
    @Operation(
        summary = "Register a device's FCM token for push (campaign alerts)",
        description = "Idempotent: registering the same token again is a no-op. 400 (1009) for a blank or malformed token.",
    )
    fun register(@RequestBody request: DeviceRegistrationDto): ResponseEntity<Unit> {
        registerDeviceUseCase(request.token)
        return ResponseEntity.noContent().build()
    }
}

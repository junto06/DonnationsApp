package com.donnations.backend.http

import com.donnations.backend.domain.usecase.GetHomeUseCase
import com.donnations.backend.http.dto.HomeDto
import com.donnations.backend.http.mapper.toDto
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/home")
class HomeController(
    private val getHomeUseCase: GetHomeUseCase,
) {
    @GetMapping
    @Operation(summary = "Home screen: category tabs, featured carousel and remaining campaigns")
    fun get(): HomeDto = getHomeUseCase().toDto()
}

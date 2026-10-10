package com.donnations.backend.http

import com.donnations.backend.domain.model.CampaignId
import com.donnations.backend.domain.usecase.GetCampaignUseCase
import com.donnations.backend.domain.usecase.GetCampaignsUseCase
import com.donnations.backend.http.dto.CampaignDetailDto
import com.donnations.backend.http.dto.CampaignSummaryDto
import com.donnations.backend.http.mapper.toDetailDto
import com.donnations.backend.http.mapper.toSummaryDto
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/campaigns")
class CampaignsController(
    private val getCampaignsUseCase: GetCampaignsUseCase,
    private val getCampaignUseCase: GetCampaignUseCase,
) {
    @GetMapping
    @Operation(summary = "List campaigns, newest first")
    fun getAll(): List<CampaignSummaryDto> =
        getCampaignsUseCase().map { it.toSummaryDto() }

    @GetMapping("/{id}")
    @Operation(summary = "Get a campaign by id")
    fun getById(@PathVariable id: UUID): CampaignDetailDto =
        getCampaignUseCase(CampaignId(id)).toDetailDto()
}

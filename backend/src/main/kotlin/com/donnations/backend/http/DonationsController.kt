package com.donnations.backend.http

import com.donnations.backend.domain.model.CampaignId
import com.donnations.backend.domain.usecase.GetDonationOptionsUseCase
import com.donnations.backend.domain.usecase.MakeDonationUseCase
import com.donnations.backend.http.dto.DonationDto
import com.donnations.backend.http.dto.DonationOptionsDto
import com.donnations.backend.http.dto.DonationRequestDto
import com.donnations.backend.http.mapper.toAmount
import com.donnations.backend.http.mapper.toDto
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/campaigns/{campaignId}")
class DonationsController(
    private val getDonationOptionsUseCase: GetDonationOptionsUseCase,
    private val makeDonationUseCase: MakeDonationUseCase,
) {
    @GetMapping("/donation-options")
    @Operation(summary = "Get what a donor needs to donate: campaign title, currency and suggested amounts")
    fun getOptions(@PathVariable campaignId: UUID): DonationOptionsDto =
        getDonationOptionsUseCase(CampaignId(campaignId)).toDto()

    @PostMapping("/donations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Donate to a campaign in its currency")
    fun donate(@PathVariable campaignId: UUID, @RequestBody request: DonationRequestDto): DonationDto =
        makeDonationUseCase(CampaignId(campaignId), request.toAmount()).toDto()
}

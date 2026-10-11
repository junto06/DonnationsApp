package com.donnations.backend.http

import com.donnations.backend.domain.model.CampaignId
import com.donnations.backend.domain.model.DonationId
import com.donnations.backend.domain.usecase.GetDonationOptionsUseCase
import com.donnations.backend.domain.usecase.GetDonationUseCase
import com.donnations.backend.domain.usecase.SubmitDonationUseCase
import com.donnations.backend.http.dto.DonationDto
import com.donnations.backend.http.dto.DonationOptionsDto
import com.donnations.backend.http.dto.DonationRequestDto
import com.donnations.backend.http.mapper.toAmount
import com.donnations.backend.http.mapper.toDto
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class DonationsController(
    private val getDonationOptionsUseCase: GetDonationOptionsUseCase,
    private val submitDonationUseCase: SubmitDonationUseCase,
    private val getDonationUseCase: GetDonationUseCase,
) {
    @GetMapping("/campaigns/{campaignId}/donation-options")
    @Operation(summary = "Get what a donor needs to donate: title, currency, whether it is open, suggested amounts")
    fun getOptions(@PathVariable campaignId: UUID): DonationOptionsDto =
        getDonationOptionsUseCase(CampaignId(campaignId)).toDto()

    @PostMapping("/campaigns/{campaignId}/donations")
    @Operation(
        summary = "Submit a donation; processed asynchronously, poll Location for its status",
        description = "Idempotent on donationId: 202 when created, 200 with the original donation on a retry, " +
            "409 if the id was used for a different campaign or amount.",
    )
    fun donate(@PathVariable campaignId: UUID, @RequestBody request: DonationRequestDto): ResponseEntity<DonationDto> {
        val (donation, created) =
            submitDonationUseCase(DonationId(request.donationId), CampaignId(campaignId), request.toAmount())
        return ResponseEntity.status(if (created) HttpStatus.ACCEPTED else HttpStatus.OK)
            .location(URI.create("/api/v1/donations/${donation.id.value}"))
            .body(donation.toDto())
    }

    @GetMapping("/donations/{donationId}")
    @Operation(summary = "Get a donation and its processing status")
    fun getDonation(@PathVariable donationId: UUID): DonationDto =
        getDonationUseCase(DonationId(donationId)).toDto()
}

package com.donnations.data.donation.repository

import com.donnations.core.base.HasLogger
import com.donnations.core.base.Logger
import com.donnations.core.base.logged
import com.donnations.core.model.CampaignId
import com.donnations.core.model.DonationId
import com.donnations.data.donation.remote.DonationApi
import com.donnations.data.donation.remote.dto.DonationDto
import com.donnations.data.donation.remote.dto.DonationRequestDto
import com.donnations.data.donation.remote.dto.DonationStatusDto
import com.donnations.data.donation.remote.dto.ErrorResponseDto
import com.donnations.data.donation.remote.toDomain
import com.donnations.domain.donation.exception.CampaignEndedException
import com.donnations.domain.donation.exception.CampaignNotFoundException
import com.donnations.domain.donation.exception.DonationException
import com.donnations.domain.donation.exception.InvalidDonationAmountException
import com.donnations.domain.donation.model.Donation
import com.donnations.domain.donation.model.DonationOptions
import com.donnations.domain.donation.repository.DonationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.math.BigDecimal
import javax.inject.Inject

class DonationRepositoryImpl @Inject constructor(
    private val api: DonationApi,
    private val json: Json,
    override val logger: Logger,
) : DonationRepository, HasLogger {
    override fun getDonationOptions(campaignId: CampaignId): Flow<DonationOptions> = flow {
        val options = logged("Loading donation options", ::isRejection) {
            httpCall { api.getDonationOptions(campaignId.value) }.toDomain()
        }
        emit(options)
    }

    override suspend fun submitDonation(id: DonationId, campaignId: CampaignId, amount: BigDecimal): Donation =
        logged("Submitting donation", ::isRejection) {
            httpCall { api.donate(campaignId.value, DonationRequestDto(id.value, amount.toPlainString())) }.toDomainOrThrow()
        }

    override suspend fun getDonation(id: DonationId): Donation =
        logged("Loading donation status", ::isRejection) { httpCall { api.getDonation(id.value) }.toDomainOrThrow() }

    // Backend rejections (campaign ended, amount rejected, ...) are expected, not bugs.
    private fun isRejection(e: Throwable) = e is DonationException

    // A failed donation is surfaced as the domain exception for its failure code, like an error response would be.
    private fun DonationDto.toDomainOrThrow(): Donation = when (status) {
        DonationStatusDto.FAILED ->
            throw failureCode?.toDomainExceptionOrNull() ?: IllegalStateException("Donation $id failed: $failureCode")
        else -> toDomain()
    }

    private suspend fun <T> httpCall(call: suspend () -> T): T = try {
        call()
    } catch (e: HttpException) {
        throw e.errorCode()?.toDomainExceptionOrNull() ?: e
    }

    private fun HttpException.errorCode(): String? = runCatching {
        json.decodeFromString<ErrorResponseDto>(response()?.errorBody()?.string().orEmpty()).errorCode
    }.getOrNull()

    // Backend ErrorCodes the user can act on; anything else (e.g. 1002 malformed request) is a bug and propagates.
    private fun String.toDomainExceptionOrNull(): Exception? = when (this) {
        "1004" -> CampaignNotFoundException()
        "1005" -> InvalidDonationAmountException()
        "1006" -> CampaignEndedException()
        else -> null
    }
}

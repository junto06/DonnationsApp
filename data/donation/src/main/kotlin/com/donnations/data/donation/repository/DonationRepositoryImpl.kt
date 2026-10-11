package com.donnations.data.donation.repository

import com.donnations.core.model.CampaignId
import com.donnations.data.donation.remote.DonationApi
import com.donnations.data.donation.remote.dto.DonationRequestDto
import com.donnations.data.donation.remote.dto.ErrorResponseDto
import com.donnations.data.donation.remote.toDomain
import com.donnations.domain.donation.exception.CampaignEndedException
import com.donnations.domain.donation.exception.CampaignNotFoundException
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
) : DonationRepository {
    override fun getDonationOptions(campaignId: CampaignId): Flow<DonationOptions> = flow {
        val options = try {
            api.getDonationOptions(campaignId.value)
        } catch (e: HttpException) {
            throw e.toDomainException()
        }
        emit(options.toDomain())
    }

    override suspend fun donate(campaignId: CampaignId, amount: BigDecimal): Donation {
        val donation = try {
            api.donate(campaignId.value, DonationRequestDto(amount.toPlainString()))
        } catch (e: HttpException) {
            throw e.toDomainException()
        }
        return donation.toDomain()
    }

    // Maps the backend's ErrorResponseDto.errorCode; anything else (e.g. 1002 malformed request) is a bug and propagates.
    private fun HttpException.toDomainException(): Exception {
        val errorCode = runCatching {
            json.decodeFromString<ErrorResponseDto>(response()?.errorBody()?.string().orEmpty()).errorCode
        }.getOrNull()
        return when (errorCode) {
            "1004" -> CampaignNotFoundException()
            "1005" -> InvalidDonationAmountException()
            "1006" -> CampaignEndedException()
            else -> this
        }
    }
}

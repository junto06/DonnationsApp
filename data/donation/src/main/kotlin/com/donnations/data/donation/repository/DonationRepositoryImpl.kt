package com.donnations.data.donation.repository

import com.donnations.core.model.CampaignId
import com.donnations.data.donation.remote.DonationApi
import com.donnations.data.donation.remote.toDomain
import com.donnations.domain.donation.exception.CampaignEndedException
import com.donnations.domain.donation.exception.CampaignNotFoundException
import com.donnations.domain.donation.model.DonationOptions
import com.donnations.domain.donation.repository.DonationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

class DonationRepositoryImpl @Inject constructor(
    private val api: DonationApi,
) : DonationRepository {
    override fun getDonationOptions(campaignId: CampaignId): Flow<DonationOptions> = flow {
        val options = try {
            api.getDonationOptions(campaignId.value)
        } catch (e: HttpException) {
            throw e.toDomainException()
        }
        emit(options.toDomain())
    }

    // The backend answers 404 for an unknown campaign and 409 once it has ended.
    private fun HttpException.toDomainException(): Exception = when (code()) {
        404 -> CampaignNotFoundException()
        409 -> CampaignEndedException()
        else -> this
    }
}

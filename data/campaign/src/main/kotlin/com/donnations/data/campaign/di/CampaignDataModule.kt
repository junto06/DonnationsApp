package com.donnations.data.campaign.di

import com.donnations.data.campaign.remote.CampaignApi
import com.donnations.data.campaign.repository.CampaignRepositoryImpl
import com.donnations.domain.campaign.repository.CampaignRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CampaignDataModule {

    @Binds
    abstract fun bindCampaignRepository(impl: CampaignRepositoryImpl): CampaignRepository

    companion object {
        @Provides
        @Singleton
        fun provideCampaignApi(retrofit: Retrofit): CampaignApi = retrofit.create(CampaignApi::class.java)
    }
}

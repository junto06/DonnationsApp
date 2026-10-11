package com.donnations.data.donation.di

import com.donnations.data.donation.remote.DonationApi
import com.donnations.data.donation.repository.DonationRepositoryImpl
import com.donnations.domain.donation.repository.DonationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DonationDataModule {

    @Binds
    abstract fun bindDonationRepository(impl: DonationRepositoryImpl): DonationRepository

    companion object {
        @Provides
        @Singleton
        fun provideDonationApi(retrofit: Retrofit): DonationApi = retrofit.create(DonationApi::class.java)
    }
}

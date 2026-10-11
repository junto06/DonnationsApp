package com.donnations.data.home.di

import com.donnations.data.home.remote.HomeApi
import com.donnations.data.home.repository.HomeRepositoryImpl
import com.donnations.domain.home.repository.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeDataModule {

    @Binds
    abstract fun bindHomeRepository(impl: HomeRepositoryImpl): HomeRepository

    companion object {
        @Provides
        @Singleton
        fun provideHomeApi(retrofit: Retrofit): HomeApi = retrofit.create(HomeApi::class.java)
    }
}

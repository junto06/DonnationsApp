package com.donnations.data.home.di

import android.content.Context
import androidx.room.Room
import com.donnations.data.home.local.HomeDao
import com.donnations.data.home.local.HomeDatabase
import com.donnations.data.home.remote.HomeApi
import com.donnations.data.home.repository.HomeRepositoryImpl
import com.donnations.domain.home.repository.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

        // It's only a cache of the backend, so a schema change just drops it; the next refresh refills it.
        @Provides
        @Singleton
        fun provideHomeDatabase(@ApplicationContext context: Context): HomeDatabase =
            Room.databaseBuilder(context, HomeDatabase::class.java, "home.db")
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()

        @Provides
        fun provideHomeDao(database: HomeDatabase): HomeDao = database.homeDao()
    }
}

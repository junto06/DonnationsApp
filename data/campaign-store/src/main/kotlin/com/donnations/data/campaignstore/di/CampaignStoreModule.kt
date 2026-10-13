package com.donnations.data.campaignstore.di

import android.content.Context
import androidx.room.Room
import com.donnations.data.campaignstore.CampaignDao
import com.donnations.data.campaignstore.CampaignStoreDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CampaignStoreModule {

    @Provides
    @Singleton
    fun provideCampaignStoreDatabase(@ApplicationContext context: Context): CampaignStoreDatabase =
        Room.databaseBuilder(context, CampaignStoreDatabase::class.java, "campaigns.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideCampaignDao(database: CampaignStoreDatabase): CampaignDao = database.campaignDao()
}

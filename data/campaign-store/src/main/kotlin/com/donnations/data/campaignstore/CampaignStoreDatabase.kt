package com.donnations.data.campaignstore

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CampaignEntity::class], version = 1)
abstract class CampaignStoreDatabase : RoomDatabase() {
    abstract fun campaignDao(): CampaignDao
}

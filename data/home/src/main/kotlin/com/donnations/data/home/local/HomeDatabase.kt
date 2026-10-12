package com.donnations.data.home.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HomeSnapshotEntity::class, CategoryEntity::class, CampaignEntity::class],
    version = 1,
)
abstract class HomeDatabase : RoomDatabase() {
    abstract fun homeDao(): HomeDao
}

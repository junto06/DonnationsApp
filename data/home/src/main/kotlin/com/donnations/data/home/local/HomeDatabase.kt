package com.donnations.data.home.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HomeSnapshotEntity::class, CategoryEntity::class, HomeEntryEntity::class],
    version = 2,
)
abstract class HomeDatabase : RoomDatabase() {
    abstract fun homeDao(): HomeDao
}

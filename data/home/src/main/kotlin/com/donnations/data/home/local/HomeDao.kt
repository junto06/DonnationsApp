package com.donnations.data.home.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
abstract class HomeDao {

    // replace() always rewrites the snapshot row, so watching it is enough to see every change.
    fun observeHome(): Flow<StoredHome?> = observeSnapshot().map { load() }

    @Transaction
    open suspend fun load(): StoredHome? {
        val snapshot = snapshot() ?: return null
        return StoredHome(snapshot, categories(), entries())
    }

    @Transaction
    open suspend fun replace(home: StoredHome) {
        deleteCategories()
        deleteEntries()
        insertCategories(home.categories)
        insertEntries(home.entries)
        upsertSnapshot(home.snapshot)
    }

    @Query("SELECT * FROM home_snapshot WHERE id = '${HomeSnapshotEntity.KEY_HOME_SNAPSHOT}'")
    abstract fun observeSnapshot(): Flow<HomeSnapshotEntity?>

    @Query("SELECT * FROM home_snapshot WHERE id = '${HomeSnapshotEntity.KEY_HOME_SNAPSHOT}'")
    abstract suspend fun snapshot(): HomeSnapshotEntity?

    @Query("SELECT * FROM categories ORDER BY position")
    abstract suspend fun categories(): List<CategoryEntity>

    @Query("SELECT * FROM home_entries ORDER BY section, position")
    abstract suspend fun entries(): List<HomeEntryEntity>

    @Upsert
    abstract suspend fun upsertSnapshot(snapshot: HomeSnapshotEntity)

    @Insert
    abstract suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert
    abstract suspend fun insertEntries(entries: List<HomeEntryEntity>)

    @Query("DELETE FROM categories")
    abstract suspend fun deleteCategories()

    @Query("DELETE FROM home_entries")
    abstract suspend fun deleteEntries()
}

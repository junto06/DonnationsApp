package com.donnations.data.home.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// One row: when the stored home was fetched. Replaced together with the other tables.
@Entity(tableName = "home_snapshot")
data class HomeSnapshotEntity(
    @PrimaryKey val id: String = KEY_HOME_SNAPSHOT,
    val updatedAtEpochMillis: Long,
) {
    companion object {
        const val KEY_HOME_SNAPSHOT = ":home-snapshot-key"
    }
}

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val position: Int,
    val id: String,
    val name: String,
)

enum class HomeSection { FEATURED, CAMPAIGNS }

// Which campaign goes where; the campaigns themselves are in the shared campaign store.
// The same campaign can be in both sections, so rows are keyed by section and position.
@Entity(tableName = "home_entries", primaryKeys = ["section", "position"])
data class HomeEntryEntity(
    val section: HomeSection,
    val position: Int,
    val campaignId: String,
)

data class StoredHome(
    val snapshot: HomeSnapshotEntity,
    val categories: List<CategoryEntity>,
    val entries: List<HomeEntryEntity>,
)

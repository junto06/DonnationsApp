package com.donnations.data.home.local

import androidx.room.Embedded
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

// The same campaign can be in both sections, so rows are keyed by section and position.
// goal/raised stay plain decimal strings to keep exact amounts.
@Entity(tableName = "campaigns", primaryKeys = ["section", "position"])
data class CampaignEntity(
    val section: HomeSection,
    val position: Int,
    val id: String,
    val title: String,
    val summary: String,
    val location: String,
    val categoryId: String,
    val imageUrl: String,
    val currency: String,
    val goal: String,
    val raised: String,
)

data class StoredHome(
    @Embedded val snapshot: HomeSnapshotEntity,
    val categories: List<CategoryEntity>,
    val campaigns: List<CampaignEntity>,
)

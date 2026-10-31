package com.vaultx.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.vaultx.core.model.CollectibleCategory

/**
 * Room persistence representation of a collectible asset stored in SQLite.
 * Serves as the Offline-First Single Source of Truth (SSOT).
 */
@Entity(
    tableName = "collectibles",
    indices = [
        Index(value = ["category"]),
        Index(value = ["acquisition_date"]),
    ],
)
data class CollectibleEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "category")
    val category: CollectibleCategory,

    @ColumnInfo(name = "purchase_price")
    val purchasePrice: Double,

    @ColumnInfo(name = "market_price")
    val marketPrice: Double,

    @ColumnInfo(name = "condition_grade")
    val conditionGrade: String,

    @ColumnInfo(name = "image_url")
    val imageUrl: String,

    @ColumnInfo(name = "acquisition_date")
    val acquisitionDate: Long,
)

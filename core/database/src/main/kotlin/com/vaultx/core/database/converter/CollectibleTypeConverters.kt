package com.vaultx.core.database.converter

import androidx.room.TypeConverter
import com.vaultx.core.model.CollectibleCategory

/**
 * Room TypeConverters for persisting [CollectibleCategory] safely as a String.
 */
class CollectibleTypeConverters {

    @TypeConverter
    fun fromCategory(category: CollectibleCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): CollectibleCategory =
        runCatching { CollectibleCategory.valueOf(value) }
            .getOrDefault(CollectibleCategory.GradedCard)
}

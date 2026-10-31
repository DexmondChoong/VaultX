package com.vaultx.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vaultx.core.database.converter.CollectibleTypeConverters
import com.vaultx.core.database.dao.CollectibleDao
import com.vaultx.core.database.entity.CollectibleEntity

@Database(
    entities = [CollectibleEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(CollectibleTypeConverters::class)
abstract class VaultXDatabase : RoomDatabase() {
    abstract fun collectibleDao(): CollectibleDao

    companion object {
        const val DATABASE_NAME = "vaultx_portfolio.db"
    }
}

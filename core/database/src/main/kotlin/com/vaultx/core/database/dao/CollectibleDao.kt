package com.vaultx.core.database.dao

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import androidx.room.Insert
import com.vaultx.core.database.entity.CollectibleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `collectibles` table.
 * Exposes reactive [Flow] queries so any local or remote mutation immediately notifies observers.
 */
@Dao
interface CollectibleDao {

    @Query("SELECT * FROM collectibles ORDER BY acquisition_date DESC")
    fun getAll(): Flow<List<CollectibleEntity>>

    @Query("SELECT * FROM collectibles WHERE id = :id LIMIT 1")
    fun getById(id: String): Flow<CollectibleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: CollectibleEntity)

    @Upsert
    suspend fun upsertAll(entities: List<CollectibleEntity>)

    @Query("UPDATE collectibles SET market_price = :newMarketPrice WHERE id = :id")
    suspend fun updatePrice(id: String, newMarketPrice: Double): Int
}

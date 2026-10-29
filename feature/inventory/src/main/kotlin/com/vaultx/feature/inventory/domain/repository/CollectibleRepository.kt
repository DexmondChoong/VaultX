package com.vaultx.feature.inventory.domain.repository

import com.vaultx.core.model.CollectibleItem
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for accessing and synchronizing collectible inventory data.
 * Follows the Dependency Inversion Principle (DIP): defined in the Domain layer and
 * implemented by the Data layer.
 */
interface CollectibleRepository {
    fun getCollectiblesStream(): Flow<List<CollectibleItem>>
    fun getCollectibleById(id: String): Flow<CollectibleItem?>
    suspend fun syncMarketPrices(): Result<Unit>
    suspend fun saveCollectible(item: CollectibleItem): Result<Unit>
}

package com.vaultx.feature.inventory.data.repository

import com.vaultx.core.database.dao.CollectibleDao
import com.vaultx.core.database.mapper.toDomainModel
import com.vaultx.core.database.mapper.toEntity
import com.vaultx.core.model.CollectibleItem
import com.vaultx.core.network.api.MarketFeedApi
import com.vaultx.feature.inventory.domain.repository.CollectibleRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-First implementation of [CollectibleRepository].
 *
 * Architectural Guarantees:
 * 1. **Single Source of Truth (SSOT)**: All UI/Domain reads exclusively observe [CollectibleDao] streams.
 * 2. **Unidirectional Sync**: Network responses from [MarketFeedApi] never return directly to the UI;
 *    they are written into Room, which then triggers reactive emissions to active collectors.
 * 3. **Coroutine Safety**: Rethrows [CancellationException] so structured concurrency is never broken.
 */
@Singleton
class CollectibleRepositoryImpl @Inject constructor(
    private val collectibleDao: CollectibleDao,
    private val marketFeedApi: MarketFeedApi,
) : CollectibleRepository {

    override fun getCollectiblesStream(): Flow<List<CollectibleItem>> = collectibleDao.getAll().map { entities ->
        entities.map { it.toDomainModel() }
    }

    override fun getCollectibleById(id: String): Flow<CollectibleItem?> = collectibleDao.getById(id).map { entity ->
        entity?.toDomainModel()
    }

    override suspend fun syncMarketPrices(): Result<Unit> = try {
        val priceUpdates = marketFeedApi.getMarketPriceUpdates()
        priceUpdates.forEach { update ->
            collectibleDao.updatePrice(
                id = update.itemId,
                newMarketPrice = update.latestMarketPrice,
            )
        }
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun saveCollectible(item: CollectibleItem): Result<Unit> = try {
        collectibleDao.insertOrReplace(item.toEntity())
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

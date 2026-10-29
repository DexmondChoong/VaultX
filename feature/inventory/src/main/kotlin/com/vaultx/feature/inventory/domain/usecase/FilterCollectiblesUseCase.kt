package com.vaultx.feature.inventory.domain.usecase

import com.vaultx.core.model.CollectibleCategory
import com.vaultx.core.model.CollectibleItem
import com.vaultx.feature.inventory.domain.repository.CollectibleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

enum class SortOption {
    PRICE,
    DATE,
    PROFIT_LOSS
}

enum class SortDirection {
    ASCENDING,
    DESCENDING
}

/**
 * Single-responsibility UseCase that filters collectibles by [CollectibleCategory] (optional)
 * and sorts them by [SortOption] and [SortDirection].
 */
class FilterCollectiblesUseCase @Inject constructor(
    private val repository: CollectibleRepository
) {
    operator fun invoke(
        category: CollectibleCategory? = null,
        sortOption: SortOption = SortOption.DATE,
        sortDirection: SortDirection = SortDirection.DESCENDING
    ): Flow<List<CollectibleItem>> {
        return repository.getCollectiblesStream().map { items ->
            val filtered = if (category != null) {
                items.filter { it.category == category }
            } else {
                items
            }

            val comparator: Comparator<CollectibleItem> = when (sortOption) {
                SortOption.PRICE -> compareBy { it.marketPrice }
                SortOption.DATE -> compareBy { it.acquisitionDate }
                SortOption.PROFIT_LOSS -> compareBy { it.unrealizedProfitLoss }
            }

            when (sortDirection) {
                SortDirection.ASCENDING -> filtered.sortedWith(comparator)
                SortDirection.DESCENDING -> filtered.sortedWith(comparator.reversed())
            }
        }
    }
}

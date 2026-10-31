package com.vaultx.feature.inventory.domain.usecase

import com.vaultx.core.model.PortfolioSummary
import com.vaultx.feature.inventory.domain.repository.CollectibleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Single-responsibility UseCase that consumes [CollectibleRepository.getCollectiblesStream]
 * and reactively computes the aggregated [PortfolioSummary].
 */
class GetPortfolioSummaryUseCase @Inject constructor(
    private val repository: CollectibleRepository,
) {
    operator fun invoke(): Flow<PortfolioSummary> {
        return repository.getCollectiblesStream().map { items ->
            if (items.isEmpty()) {
                return@map PortfolioSummary.EMPTY
            }

            val totalValuation = items.sumOf { it.marketPrice }
            val totalCost = items.sumOf { it.purchasePrice }
            val unrealizedProfitLoss = totalValuation - totalCost
            val returnPercentage = if (totalCost > 0.0) {
                (unrealizedProfitLoss / totalCost) * 100.0
            } else {
                0.0
            }

            PortfolioSummary(
                totalValuation = totalValuation,
                totalCost = totalCost,
                unrealizedProfitLoss = unrealizedProfitLoss,
                returnPercentage = returnPercentage,
                itemCount = items.size,
            )
        }
    }
}

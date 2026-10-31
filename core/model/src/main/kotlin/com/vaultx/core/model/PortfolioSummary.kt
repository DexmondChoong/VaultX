package com.vaultx.core.model

/**
 * Aggregated portfolio valuation metrics computed reactively across all collectibles.
 */
data class PortfolioSummary(
    val totalValuation: Double,
    val totalCost: Double,
    val unrealizedProfitLoss: Double,
    val returnPercentage: Double,
    val itemCount: Int,
) {
    companion object {
        val EMPTY = PortfolioSummary(
            totalValuation = 0.0,
            totalCost = 0.0,
            unrealizedProfitLoss = 0.0,
            returnPercentage = 0.0,
            itemCount = 0,
        )
    }
}

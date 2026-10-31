package com.vaultx.core.model

import java.util.UUID

/**
 * Pure Kotlin domain representation of a tracked collectible asset in the VaultX portfolio.
 * Free of any Android SDK dependencies.
 */
data class CollectibleItem(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val category: CollectibleCategory,
    val purchasePrice: Double,
    val marketPrice: Double,
    val conditionGrade: String,
    val imageUrl: String,
    val acquisitionDate: Long,
) {
    /**
     * Computed unrealized profit/loss for this individual asset.
     */
    val unrealizedProfitLoss: Double
        get() = marketPrice - purchasePrice

    /**
     * Computed return percentage for this asset. Returns 0.0 when purchasePrice is 0.0 to avoid NaN/Infinity.
     */
    val returnPercentage: Double
        get() = if (purchasePrice > 0.0) {
            ((marketPrice - purchasePrice) / purchasePrice) * 100.0
        } else {
            0.0
        }
}

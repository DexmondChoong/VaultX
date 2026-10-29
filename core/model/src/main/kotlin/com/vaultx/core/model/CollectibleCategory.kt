package com.vaultx.core.model

import kotlinx.serialization.Serializable

/**
 * Pure Kotlin domain enumeration representing collectible asset categories.
 */
@Serializable
enum class CollectibleCategory {
    GradedCard,
    SealedBox,
    Comic
}

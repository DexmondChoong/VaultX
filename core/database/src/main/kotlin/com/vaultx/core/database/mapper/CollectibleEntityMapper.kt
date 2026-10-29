package com.vaultx.core.database.mapper

import com.vaultx.core.database.entity.CollectibleEntity
import com.vaultx.core.model.CollectibleItem
import java.util.UUID

/**
 * Bidirectional mapper extensions between Room [CollectibleEntity] and domain [CollectibleItem].
 */
fun CollectibleEntity.toDomainModel(): CollectibleItem = CollectibleItem(
    id = UUID.fromString(id),
    name = name,
    category = category,
    purchasePrice = purchasePrice,
    marketPrice = marketPrice,
    conditionGrade = conditionGrade,
    imageUrl = imageUrl,
    acquisitionDate = acquisitionDate
)

fun CollectibleItem.toEntity(): CollectibleEntity = CollectibleEntity(
    id = id.toString(),
    name = name,
    category = category,
    purchasePrice = purchasePrice,
    marketPrice = marketPrice,
    conditionGrade = conditionGrade,
    imageUrl = imageUrl,
    acquisitionDate = acquisitionDate
)

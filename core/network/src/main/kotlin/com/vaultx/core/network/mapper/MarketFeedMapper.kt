package com.vaultx.core.network.mapper

import com.vaultx.core.model.CollectibleCategory
import com.vaultx.core.model.CollectibleItem
import com.vaultx.core.network.dto.CollectibleMetadataDto
import java.util.UUID

/**
 * Maps network [CollectibleMetadataDto] to pure domain [CollectibleItem].
 */
fun CollectibleMetadataDto.toDomainModel(): CollectibleItem {
    val parsedCategory = runCatching {
        CollectibleCategory.valueOf(category)
    }.getOrDefault(CollectibleCategory.GradedCard)

    val parsedId = runCatching {
        UUID.fromString(id)
    }.getOrElse { UUID.randomUUID() }

    return CollectibleItem(
        id = parsedId,
        name = name,
        category = parsedCategory,
        purchasePrice = purchasePrice,
        marketPrice = marketPrice,
        conditionGrade = conditionGrade,
        imageUrl = imageUrl,
        acquisitionDate = acquisitionDate
    )
}

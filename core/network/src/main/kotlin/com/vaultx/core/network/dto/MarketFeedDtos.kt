package com.vaultx.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network DTO representing real-time market valuation updates from the pricing feed.
 */
@Serializable
data class MarketPriceUpdateDto(
    @SerialName("item_id")
    val itemId: String,

    @SerialName("latest_market_price")
    val latestMarketPrice: Double,

    @SerialName("currency")
    val currency: String = "USD",

    @SerialName("updated_at_epoch_ms")
    val updatedAtEpochMs: Long
)

/**
 * Network DTO representing collectible metadata and pricing returned from a catalog lookup or feed.
 */
@Serializable
data class CollectibleMetadataDto(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("category")
    val category: String,

    @SerialName("purchase_price")
    val purchasePrice: Double = 0.0,

    @SerialName("market_price")
    val marketPrice: Double,

    @SerialName("condition_grade")
    val conditionGrade: String,

    @SerialName("image_url")
    val imageUrl: String,

    @SerialName("acquisition_date")
    val acquisitionDate: Long
)

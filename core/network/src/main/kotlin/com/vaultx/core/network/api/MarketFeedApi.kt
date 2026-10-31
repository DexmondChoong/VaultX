package com.vaultx.core.network.api

import com.vaultx.core.network.dto.CollectibleMetadataDto
import com.vaultx.core.network.dto.MarketPriceUpdateDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service interface for real-time market pricing feeds and collectible metadata lookups.
 */
interface MarketFeedApi {

    /**
     * Fetches the latest market price quotes for tracked portfolio items.
     */
    @GET("v1/market/prices")
    suspend fun getMarketPriceUpdates(): List<MarketPriceUpdateDto>

    /**
     * Fetches full metadata & valuation for a specific collectible by catalog ID or barcode.
     */
    @GET("v1/catalog/collectibles/{id}")
    suspend fun getCollectibleMetadata(
        @Path("id") id: String,
    ): CollectibleMetadataDto

    /**
     * Searches catalog metadata by query string (e.g., from Vision Scanner OCR or search bar).
     */
    @GET("v1/catalog/search")
    suspend fun searchCatalog(
        @Query("q") query: String,
    ): List<CollectibleMetadataDto>
}

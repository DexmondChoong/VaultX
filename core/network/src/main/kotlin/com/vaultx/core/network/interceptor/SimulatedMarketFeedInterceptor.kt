package com.vaultx.core.network.interceptor

import com.vaultx.core.network.dto.CollectibleMetadataDto
import com.vaultx.core.network.dto.MarketPriceUpdateDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Showcase OkHttp Interceptor that simulates a live financial market pricing feed
 * for `api.vaultx.io` endpoints when running in standalone/demo mode.
 *
 * Why this pattern matters for an architectural showcase:
 * - Preserves the full Retrofit2 -> OkHttp3 -> Kotlinx Serialization -> Repository -> Room pipeline.
 * - Every pull-to-refresh or sync action simulates realistic market price fluctuations (±6%),
 *   allowing reviewers to observe reactive Room + StateFlow UI updates in real time.
 */
class SimulatedMarketFeedInterceptor(
    private val json: Json,
) : Interceptor {

    private val currentPrices = mutableMapOf(
        "11111111-1111-1111-1111-111111111111" to 12400.0,
        "22222222-2222-2222-2222-222222222222" to 690.0,
        "33333333-3333-3333-3333-333333333333" to 2850.0,
        "44444444-4444-4444-4444-444444444444" to 1180.0,
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        // Simulate realistic network latency (350ms)
        Thread.sleep(350L)

        val responseJson = when {
            path.endsWith("/v1/market/prices") -> {
                val now = System.currentTimeMillis()
                val updates = currentPrices.map { (id, basePrice) ->
                    // Fluctuate market price between -4.5% and +5.5% on each refresh
                    val deltaPercent = Random.nextDouble(from = -0.045, until = 0.055)
                    val updatedPrice = ((basePrice * (1.0 + deltaPercent)) * 100.0).roundToInt() / 100.0
                    currentPrices[id] = updatedPrice

                    MarketPriceUpdateDto(
                        itemId = id,
                        latestMarketPrice = updatedPrice,
                        currency = "USD",
                        updatedAtEpochMs = now,
                    )
                }
                json.encodeToString(updates)
            }

            path.contains("/v1/catalog/collectibles/") -> {
                val id = path.substringAfterLast("/")
                val metadata = CollectibleMetadataDto(
                    id = id,
                    name = "1999 Base Set Charizard Holo 1st Ed",
                    category = "GradedCard",
                    purchasePrice = 8500.0,
                    marketPrice = currentPrices[id] ?: 12400.0,
                    conditionGrade = "PSA 10",
                    imageUrl = "https://example.com/charizard.png",
                    acquisitionDate = System.currentTimeMillis(),
                )
                json.encodeToString(metadata)
            }

            else -> "[]"
        }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_2)
            .code(200)
            .message("OK (Simulated Market Feed)")
            .body(responseJson.toResponseBody("application/json".toMediaType()))
            .addHeader("Content-Type", "application/json")
            .build()
    }
}

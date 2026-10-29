package com.vaultx.feature.inventory.domain.usecase

import app.cash.turbine.test
import com.vaultx.core.model.CollectibleCategory
import com.vaultx.core.model.CollectibleItem
import com.vaultx.core.model.PortfolioSummary
import com.vaultx.feature.inventory.domain.repository.CollectibleRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID

class GetPortfolioSummaryUseCaseTest {

    private lateinit var repository: CollectibleRepository
    private lateinit var getPortfolioSummaryUseCase: GetPortfolioSummaryUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk()
        getPortfolioSummaryUseCase = GetPortfolioSummaryUseCase(repository)
    }

    @Test
    @DisplayName("Given empty collection, When invoked, Then emits zeroed PortfolioSummary")
    fun `empty collection emits zeroed portfolio summary`() = runTest {
        every { repository.getCollectiblesStream() } returns flowOf(emptyList())

        getPortfolioSummaryUseCase().test {
            val summary = awaitItem()
            assertEquals(PortfolioSummary.EMPTY, summary)
            assertEquals(0.0, summary.totalValuation, 1e-6)
            assertEquals(0.0, summary.totalCost, 1e-6)
            assertEquals(0.0, summary.unrealizedProfitLoss, 1e-6)
            assertEquals(0.0, summary.returnPercentage, 1e-6)
            assertEquals(0, summary.itemCount)
            awaitComplete()
        }

        verify(exactly = 1) { repository.getCollectiblesStream() }
    }

    @Test
    @DisplayName("Given collection with net positive gain, When invoked, Then computes accurate valuation and positive return percentage")
    fun `collection with positive gain computes accurate metrics`() = runTest {
        val items = listOf(
            createItem(
                name = "1999 Charizard Holo PSA 10",
                category = CollectibleCategory.GradedCard,
                purchasePrice = 1000.0,
                marketPrice = 1500.0
            ),
            createItem(
                name = "Pokemon Base Set Booster Box",
                category = CollectibleCategory.SealedBox,
                purchasePrice = 500.0,
                marketPrice = 900.0
            )
        )
        every { repository.getCollectiblesStream() } returns flowOf(items)

        getPortfolioSummaryUseCase().test {
            val summary = awaitItem()
            // Total Cost = 1500.0, Total Valuation = 2400.0, Profit = +900.0, Return = (900 / 1500) * 100 = +60.0%
            assertEquals(2400.0, summary.totalValuation, 1e-6)
            assertEquals(1500.0, summary.totalCost, 1e-6)
            assertEquals(900.0, summary.unrealizedProfitLoss, 1e-6)
            assertEquals(60.0, summary.returnPercentage, 1e-6)
            assertEquals(2, summary.itemCount)
            awaitComplete()
        }
    }

    @Test
    @DisplayName("Given collection with net negative loss, When invoked, Then computes negative unrealized PnL and negative return percentage")
    fun `collection with negative loss computes negative metrics`() = runTest {
        val items = listOf(
            createItem(
                name = "Amazing Fantasy #15 CGC 4.0",
                category = CollectibleCategory.Comic,
                purchasePrice = 2000.0,
                marketPrice = 1500.0
            ),
            createItem(
                name = "Modern Promo PSA 9",
                category = CollectibleCategory.GradedCard,
                purchasePrice = 500.0,
                marketPrice = 250.0
            )
        )
        every { repository.getCollectiblesStream() } returns flowOf(items)

        getPortfolioSummaryUseCase().test {
            val summary = awaitItem()
            // Total Cost = 2500.0, Total Valuation = 1750.0, Loss = -750.0, Return = (-750 / 2500) * 100 = -30.0%
            assertEquals(1750.0, summary.totalValuation, 1e-6)
            assertEquals(2500.0, summary.totalCost, 1e-6)
            assertEquals(-750.0, summary.unrealizedProfitLoss, 1e-6)
            assertEquals(-30.0, summary.returnPercentage, 1e-6)
            assertEquals(2, summary.itemCount)
            awaitComplete()
        }
    }

    @Test
    @DisplayName("Given exclusively zero-cost items (e.g. gifts or promo pulls), When invoked, Then avoids division by zero and returns 0.0% return")
    fun `zero cost items avoid division by zero`() = runTest {
        val items = listOf(
            createItem(
                name = "Gifted Trophy Card",
                category = CollectibleCategory.GradedCard,
                purchasePrice = 0.0,
                marketPrice = 850.0
            ),
            createItem(
                name = "Tournament Prize Box",
                category = CollectibleCategory.SealedBox,
                purchasePrice = 0.0,
                marketPrice = 350.0
            )
        )
        every { repository.getCollectiblesStream() } returns flowOf(items)

        getPortfolioSummaryUseCase().test {
            val summary = awaitItem()
            assertEquals(1200.0, summary.totalValuation, 1e-6)
            assertEquals(0.0, summary.totalCost, 1e-6)
            assertEquals(1200.0, summary.unrealizedProfitLoss, 1e-6)
            assertEquals(0.0, summary.returnPercentage, 1e-6)
            assertEquals(2, summary.itemCount)
            awaitComplete()
        }
    }

    @Test
    @DisplayName("Given upstream stream updates over time, When new items are added, Then reactively emits updated PortfolioSummary")
    fun `reactive stream emits updated summaries as inventory changes`() = runTest {
        val upstream = MutableSharedFlow<List<CollectibleItem>>()
        every { repository.getCollectiblesStream() } returns upstream

        val firstItem = createItem(
            name = "Item 1",
            category = CollectibleCategory.Comic,
            purchasePrice = 100.0,
            marketPrice = 120.0
        )
        val secondItem = createItem(
            name = "Item 2",
            category = CollectibleCategory.SealedBox,
            purchasePrice = 100.0,
            marketPrice = 180.0
        )

        getPortfolioSummaryUseCase().test {
            upstream.emit(emptyList())
            assertEquals(PortfolioSummary.EMPTY, awaitItem())

            upstream.emit(listOf(firstItem))
            val firstSummary = awaitItem()
            assertEquals(120.0, firstSummary.totalValuation, 1e-6)
            assertEquals(20.0, firstSummary.returnPercentage, 1e-6)
            assertEquals(1, firstSummary.itemCount)

            upstream.emit(listOf(firstItem, secondItem))
            val secondSummary = awaitItem()
            assertEquals(300.0, secondSummary.totalValuation, 1e-6)
            assertEquals(200.0, secondSummary.totalCost, 1e-6)
            assertEquals(100.0, secondSummary.unrealizedProfitLoss, 1e-6)
            assertEquals(50.0, secondSummary.returnPercentage, 1e-6)
            assertEquals(2, secondSummary.itemCount)

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createItem(
        id: UUID = UUID.randomUUID(),
        name: String,
        category: CollectibleCategory,
        purchasePrice: Double,
        marketPrice: Double,
        conditionGrade: String = "PSA 10",
        imageUrl: String = "https://example.com/image.png",
        acquisitionDate: Long = 1700000000000L
    ) = CollectibleItem(
        id = id,
        name = name,
        category = category,
        purchasePrice = purchasePrice,
        marketPrice = marketPrice,
        conditionGrade = conditionGrade,
        imageUrl = imageUrl,
        acquisitionDate = acquisitionDate
    )
}

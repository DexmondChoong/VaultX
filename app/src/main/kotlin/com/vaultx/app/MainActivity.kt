package com.vaultx.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.vaultx.core.designsystem.theme.VaultXTheme
import com.vaultx.core.model.CollectibleCategory
import com.vaultx.core.model.CollectibleItem
import com.vaultx.feature.inventory.domain.repository.CollectibleRepository
import com.vaultx.feature.inventory.presentation.screen.InventoryRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Main launcher Activity hosting the VaultX Compose navigation/screen hierarchy.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var collectibleRepository: CollectibleRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        seedSamplePortfolioIfNeeded()

        setContent {
            VaultXTheme {
                InventoryRoute()
            }
        }
    }

    /**
     * Populates initial showcase data on first launch if the local Room database is empty.
     */
    private fun seedSamplePortfolioIfNeeded() {
        lifecycleScope.launch {
            val currentItems = collectibleRepository.getCollectiblesStream().first()
            if (currentItems.isEmpty()) {
                val sampleItems = listOf(
                    CollectibleItem(
                        id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
                        name = "1999 Base Set Charizard Holo 1st Ed",
                        category = CollectibleCategory.GradedCard,
                        purchasePrice = 8500.0,
                        marketPrice = 12400.0,
                        conditionGrade = "PSA 10",
                        imageUrl = "https://example.com/charizard.png",
                        acquisitionDate = System.currentTimeMillis() - 86_400_000L * 90
                    ),
                    CollectibleItem(
                        id = UUID.fromString("22222222-2222-2222-2222-222222222222"),
                        name = "Pokemon Evolving Skies Booster Box",
                        category = CollectibleCategory.SealedBox,
                        purchasePrice = 145.0,
                        marketPrice = 690.0,
                        conditionGrade = "Factory Sealed",
                        imageUrl = "https://example.com/evolving_skies.png",
                        acquisitionDate = System.currentTimeMillis() - 86_400_000L * 60
                    ),
                    CollectibleItem(
                        id = UUID.fromString("33333333-3333-3333-3333-333333333333"),
                        name = "Amazing Spider-Man #300 (1st Venom)",
                        category = CollectibleCategory.Comic,
                        purchasePrice = 2200.0,
                        marketPrice = 2850.0,
                        conditionGrade = "CGC 9.8",
                        imageUrl = "https://example.com/asm300.png",
                        acquisitionDate = System.currentTimeMillis() - 86_400_000L * 30
                    ),
                    CollectibleItem(
                        id = UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        name = "2023 One Piece Awakening of the New Era Case",
                        category = CollectibleCategory.SealedBox,
                        purchasePrice = 1400.0,
                        marketPrice = 1180.0,
                        conditionGrade = "Sealed Case",
                        imageUrl = "https://example.com/op05.png",
                        acquisitionDate = System.currentTimeMillis() - 86_400_000L * 10
                    )
                )

                sampleItems.forEach { item ->
                    collectibleRepository.saveCollectible(item)
                }
            }
        }
    }
}

package com.vaultx.feature.inventory.presentation.state

import com.vaultx.core.model.CollectibleCategory
import com.vaultx.core.model.CollectibleItem
import com.vaultx.core.model.PortfolioSummary

/**
 * UI-level filter representation mapping to domain [CollectibleCategory].
 */
enum class CategoryFilter(val displayName: String, val domainCategory: CollectibleCategory?) {
    ALL("All Assets", null),
    GRADED_CARD("Graded Cards", CollectibleCategory.GradedCard),
    SEALED_BOX("Sealed Boxes", CollectibleCategory.SealedBox),
    COMIC("Comics", CollectibleCategory.Comic),
}

/**
 * Immutable UI State representation for the Inventory Screen following Unidirectional Data Flow (UDF).
 */
sealed interface InventoryUiState {

    data object Loading : InventoryUiState

    data class Success(
        val summary: PortfolioSummary,
        val items: List<CollectibleItem>,
        val selectedFilter: CategoryFilter = CategoryFilter.ALL,
        val searchQuery: String = "",
        val isRefreshing: Boolean = false,
    ) : InventoryUiState

    data class Error(
        val message: String,
    ) : InventoryUiState
}

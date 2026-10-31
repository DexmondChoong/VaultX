package com.vaultx.feature.inventory.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultx.feature.inventory.domain.repository.CollectibleRepository
import com.vaultx.feature.inventory.domain.usecase.FilterCollectiblesUseCase
import com.vaultx.feature.inventory.domain.usecase.GetPortfolioSummaryUseCase
import com.vaultx.feature.inventory.domain.usecase.SortDirection
import com.vaultx.feature.inventory.domain.usecase.SortOption
import com.vaultx.feature.inventory.presentation.state.CategoryFilter
import com.vaultx.feature.inventory.presentation.state.InventoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Presentation layer state holder for the VaultX Inventory screen.
 *
 * Uses Unidirectional Data Flow (UDF):
 * - Exposes a single immutable [uiState] stream backed by `stateIn` with `SharingStarted.WhileSubscribed(5_000)`.
 * - Processes user intents (`onRefresh`, `onFilterSelected`, `onSearchQueryChanged`) deterministically.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InventoryViewModel @Inject constructor(
    getPortfolioSummaryUseCase: GetPortfolioSummaryUseCase,
    private val filterCollectiblesUseCase: FilterCollectiblesUseCase,
    private val collectibleRepository: CollectibleRepository,
) : ViewModel() {

    private val selectedFilterFlow = MutableStateFlow(CategoryFilter.ALL)
    private val searchQueryFlow = MutableStateFlow("")
    private val isRefreshingFlow = MutableStateFlow(false)
    private val syncErrorFlow = MutableStateFlow<String?>(null)

    private val filteredItemsStream = selectedFilterFlow.flatMapLatest { filter ->
        filterCollectiblesUseCase(
            category = filter.domainCategory,
            sortOption = SortOption.DATE,
            sortDirection = SortDirection.DESCENDING,
        )
    }

    val uiState: StateFlow<InventoryUiState> = combine(
        getPortfolioSummaryUseCase(),
        filteredItemsStream,
        selectedFilterFlow,
        searchQueryFlow,
        isRefreshingFlow,
    ) { summary, items, selectedFilter, searchQuery, isRefreshing ->
        val normalizedQuery = searchQuery.trim()
        val matchingItems = if (normalizedQuery.isEmpty()) {
            items
        } else {
            items.filter { item ->
                item.name.contains(normalizedQuery, ignoreCase = true) ||
                    item.conditionGrade.contains(normalizedQuery, ignoreCase = true)
            }
        }

        InventoryUiState.Success(
            summary = summary,
            items = matchingItems,
            selectedFilter = selectedFilter,
            searchQuery = searchQuery,
            isRefreshing = isRefreshing,
        ) as InventoryUiState
    }
        .combine(syncErrorFlow) { baseState, errorMessage ->
            if (errorMessage != null && baseState is InventoryUiState.Success && baseState.items.isEmpty()) {
                InventoryUiState.Error(message = errorMessage)
            } else {
                baseState
            }
        }
        .catch { throwable ->
            emit(
                InventoryUiState.Error(
                    message = throwable.localizedMessage ?: "An unexpected error occurred loading your vault.",
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = InventoryUiState.Loading,
        )

    /**
     * Triggers an Offline-First market price sync with the remote pricing feed.
     */
    fun onRefresh() {
        if (isRefreshingFlow.value) return

        viewModelScope.launch {
            isRefreshingFlow.value = true
            syncErrorFlow.value = null

            collectibleRepository.syncMarketPrices()
                .onFailure { error ->
                    syncErrorFlow.value = error.localizedMessage
                        ?: "Unable to sync latest market prices. Showing cached offline valuation."
                }

            isRefreshingFlow.value = false
        }
    }

    /**
     * Updates the selected category chip filter.
     */
    fun onFilterSelected(filter: CategoryFilter) {
        selectedFilterFlow.value = filter
    }

    /**
     * Updates the search query used to filter collectibles by name or grade.
     */
    fun onSearchQueryChanged(query: String) {
        searchQueryFlow.value = query
    }

    /**
     * Clears any transient sync error state and retries loading/syncing.
     */
    fun onDismissError() {
        syncErrorFlow.value = null
    }
}

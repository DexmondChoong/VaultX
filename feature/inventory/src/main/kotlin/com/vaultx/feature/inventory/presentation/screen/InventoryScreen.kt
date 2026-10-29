package com.vaultx.feature.inventory.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultx.core.model.CollectibleItem
import com.vaultx.feature.inventory.presentation.components.CollectibleItemCard
import com.vaultx.feature.inventory.presentation.components.PortfolioOverviewCard
import com.vaultx.feature.inventory.presentation.state.CategoryFilter
import com.vaultx.feature.inventory.presentation.state.InventoryUiState
import com.vaultx.feature.inventory.presentation.viewmodel.InventoryViewModel

/**
 * Stateful route composable that binds [InventoryViewModel] with lifecycle-aware state collection.
 */
@Composable
fun InventoryRoute(
    onItemClick: (CollectibleItem) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    InventoryScreen(
        uiState = uiState,
        onRefresh = viewModel::onRefresh,
        onFilterSelected = viewModel::onFilterSelected,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onItemClick = onItemClick,
        modifier = modifier
    )
}

/**
 * Stateless Inventory Screen following Unidirectional Data Flow (UDF).
 * Includes Material 3 PullToRefreshBox, Search Bar, Category FilterChips,
 * PortfolioOverviewCard, and Empty/Error states.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    uiState: InventoryUiState,
    onRefresh: () -> Unit,
    onFilterSelected: (CategoryFilter) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onItemClick: (CollectibleItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "VaultX Portfolio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Market Prices"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        when (uiState) {
            is InventoryUiState.Loading -> {
                LoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            is InventoryUiState.Error -> {
                ErrorState(
                    message = uiState.message,
                    onRetry = onRefresh,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            is InventoryUiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    InventoryContent(
                        state = uiState,
                        onFilterSelected = onFilterSelected,
                        onSearchQueryChanged = onSearchQueryChanged,
                        onItemClick = onItemClick
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryContent(
    state: InventoryUiState.Success,
    onFilterSelected: (CategoryFilter) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onItemClick: (CollectibleItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Portfolio Summary Card
        item(key = "portfolio_overview") {
            PortfolioOverviewCard(summary = state.summary)
        }

        // 2. Search Bar
        item(key = "search_bar") {
            InventorySearchBar(
                query = state.searchQuery,
                onQueryChanged = onSearchQueryChanged
            )
        }

        // 3. Category Filter Chips
        item(key = "filter_chips") {
            CategoryFilterChipsRow(
                selectedFilter = state.selectedFilter,
                onFilterSelected = onFilterSelected
            )
        }

        // 4. Collectibles List or Empty State
        if (state.items.isEmpty()) {
            item(key = "empty_state") {
                EmptyInventoryState(
                    selectedFilter = state.selectedFilter,
                    searchQuery = state.searchQuery,
                    onResetFilters = {
                        onFilterSelected(CategoryFilter.ALL)
                        onSearchQueryChanged("")
                    }
                )
            }
        } else {
            items(
                items = state.items,
                key = { item -> item.id.toString() }
            ) { collectible ->
                CollectibleItemCard(
                    item = collectible,
                    onClick = onItemClick
                )
            }
        }
    }
}

@Composable
private fun InventorySearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text("Search by collectible name or grade (e.g. PSA 10)...")
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search query"
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun CategoryFilterChipsRow(
    selectedFilter: CategoryFilter,
    onFilterSelected: (CategoryFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = CategoryFilter.entries,
            key = { it.name }
        ) { filter ->
            FilterChip(
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = filter.displayName,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    }
}

@Composable
private fun EmptyInventoryState(
    selectedFilter: CategoryFilter,
    searchQuery: String,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val isFiltered = selectedFilter != CategoryFilter.ALL || searchQuery.isNotBlank()

        Text(
            text = if (isFiltered) "No Matching Collectibles" else "Your Vault is Empty",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isFiltered) {
                "No items match your active category filter or search query."
            } else {
                "Add or scan your first graded card, sealed box, or comic to start tracking real-time valuation."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (isFiltered) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onResetFilters) {
                Text("Reset Filters")
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Decrypting Vault Portfolio...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Unable to Load Portfolio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = onRetry) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null
                    )
                    Text("Retry Sync")
                }
            }
        }
    }
}

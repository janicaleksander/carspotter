package com.example.carspotter.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carspotter.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onUserCarClick: (String) -> Unit,
    onDreamCarClick: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onCategorySelected = viewModel::selectCategory,
        onFilterModeSelected = viewModel::selectFilterMode,
        onBrowseModeSelected = viewModel::selectBrowseMode,
        onUserCarClick = onUserCarClick,
        onDreamCarClick = onDreamCarClick,
        onHeartClick = viewModel::onHeartClick,
    )

}

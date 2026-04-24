package com.example.carspotter.ui.garage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carspotter.viewmodels.GarageViewModel

@Composable
fun GarageScreen(
    viewModel: GarageViewModel,
    onCarClick: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GarageContent(
        uiState = uiState,
        onCategorySelected = viewModel::selectCategory,
        onBrandSelected = viewModel::selectBrand,
        onFilterModeSelected = viewModel::selectFilterMode,
        onSortSelected = viewModel::selectSort,
        onCarClick = onCarClick,
        onHeartClick = viewModel::onHeartClick,
    )
}

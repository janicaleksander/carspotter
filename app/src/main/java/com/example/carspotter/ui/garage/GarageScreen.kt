package com.example.carspotter.ui.garage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carspotter.viewmodels.GarageViewModel

//sort by car price also
@Composable
fun GarageScreen(
    viewModel: GarageViewModel,
    onCarClick : (String) -> Unit,
    onHeartClick : (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GarageContent(
        uiState = uiState,
        onCategorySelected = viewModel::selectCategory,
        onBrandSelected = viewModel::selectBrand,
        isSelectedFavourite = viewModel::isSelectedFavourite,
        onCarClick = onCarClick,
        onHeartClick = onHeartClick
    )
}

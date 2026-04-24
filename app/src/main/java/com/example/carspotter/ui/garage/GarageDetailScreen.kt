package com.example.carspotter.ui.garage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carspotter.viewmodels.GarageDetailViewModel

@Composable
fun GarageDetailScreen(
    viewModel: GarageDetailViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFavourite by viewModel.isFavourite.collectAsStateWithLifecycle()

    GarageDetailContent(
        uiState = uiState,
        isFavourite = isFavourite,
        onToggleFavourite = viewModel::toggleFavourite,
        onRemoveFromGarage = viewModel::removeFromGarage,
        onNavigateBack = onNavigateBack,
        onDeleted = onNavigateBack,
    )
}

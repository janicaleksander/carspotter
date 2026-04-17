package com.example.carspotter.ui.tops

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carspotter.viewmodels.TopsDetailViewModel

@Composable
fun TopsDetailScreen(
    viewModel: TopsDetailViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDream by viewModel.isDream.collectAsStateWithLifecycle()

    TopsDetailContent(
        uiState = uiState,
        ifUserHasDream = isDream,
        onAddToDream = viewModel::addUserDream,
        onRemoveFromDream = viewModel::deleteUserDream,
        onNavigateBack = onNavigateBack,
    )
}
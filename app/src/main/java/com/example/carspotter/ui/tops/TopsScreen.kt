package com.example.carspotter.ui.tops

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carspotter.viewmodels.TopsViewModel

@Composable
fun TopsScreen(viewModel: TopsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TopsContent(
        uiState = uiState,
        onCategorySelected = viewModel::selectCategory,
        onCarClick = { /* TODO: navigate to car detail */ }
    )
}

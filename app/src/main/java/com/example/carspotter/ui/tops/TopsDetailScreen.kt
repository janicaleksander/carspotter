package com.example.carspotter.ui.tops

import com.example.carspotter.viewmodels.TopsDetailViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TopsDetailScreen(
    viewModel: TopsDetailViewModel
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDream by viewModel.isDream.collectAsStateWithLifecycle()
    TopsDetailContent(
        uiState=uiState,
        ifUserHasDream = isDream,
        onAddToDream = viewModel::addUserDream,
        onRemoveFromDream = viewModel::deleteUserDream
    )
}
package com.example.carspotter.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carspotter.viewmodels.AuthViewModel
import com.example.carspotter.viewmodels.SettingsViewModel


// SettingsScreen.kt
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    authViewModel: AuthViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val randomPhoto by viewModel.random_photo.collectAsStateWithLifecycle()


    SettingsContent(
        settings = settings,
        onPermissionsClick = { /* TODO */ },
        onLogoutClick = { authViewModel.logout() },
        imageUrl = randomPhoto ?: "https://via.placeholder.com/150"
    )
}
package com.example.carspotter.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.carspotter.models.Settings
import com.example.carspotter.viewmodels.SettingsViewModel
import java.time.format.DateTimeFormatter


// SettingsScreen.kt
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    SettingsContent(
        settings = settings,
        onPermissionsClick = { /* TODO */ },
        imageUrl = "https://images.unsplash.com/photo-1605559424843-9e4c228bf1c2?q=80&w=3840&auto=format&fit=crop"
    )
}
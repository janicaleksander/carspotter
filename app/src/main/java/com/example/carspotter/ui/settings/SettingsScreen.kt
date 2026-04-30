package com.example.carspotter.ui.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carspotter.viewmodels.AuthViewModel
import com.example.carspotter.viewmodels.SettingsViewModel


@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val randomPhoto by viewModel.random_photo.collectAsStateWithLifecycle()


    SettingsContent(
        settings = settings,
        onPermissionsClick = {
            val packageUri = Uri.fromParts("package", context.packageName, null)
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
            context.startActivity(intent)
        },
        onLogoutClick = { authViewModel.logout() },
        imageUrl = randomPhoto ?: "https://via.placeholder.com/150"
    )
}

package com.example.carspotter.ui.login

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carspotter.viewmodels.AuthViewModel

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.errorId) {
        uiState.errorMessage?.let {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        AuthContent(
            innerPadding = innerPadding,
            uiState = uiState,
            onEmailChange = viewModel::updateEmail,
            onPasswordChange = viewModel::updatePassword,
            onNicknameChange = viewModel::updateNickname,
            onPasswordVisibilityToggle = viewModel::togglePasswordVisibility,
            onModeToggle = viewModel::toggleAuthMode,
            onSubmit = viewModel::submit,
        )
    }
}

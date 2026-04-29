package com.example.carspotter.ui.new_spot

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carspotter.ui.components.MapPickerDialog
import com.example.carspotter.viewmodels.NewSpotViewModel
import com.example.carspotter.viewmodels.SaveSpotState
import java.io.File

@Composable
fun NewSpotScreen(
    viewModel: NewSpotViewModel,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var pendingCameraFile by remember { mutableStateOf<File?>(null) }
    var showMapPicker by remember { mutableStateOf(false) }
    // Latch — we want the overlay to keep playing even after the VM resets
    // saveState back to Idle on consume.
    var showSuccessOverlay by remember { mutableStateOf(false) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        val file = pendingCameraFile
        if (success && file != null) viewModel.addPhoto(file.absolutePath)
        else file?.delete()
        pendingCameraFile = null
    }

    val pickImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10),
    ) { uris ->
        uris.forEach { copyUriToCache(context, it, "photo", ".jpg")?.let(viewModel::addPhoto) }
    }

    val pickVideoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        copyUriToCache(context, uri, "video", ".mp4")?.let(viewModel::addVideo)
    }

    val pickAudioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        copyUriToCache(context, uri, "audio", ".m4a")?.let(viewModel::setAudio)
    }

    LaunchedEffect(uiState.saveState) {
        if (uiState.saveState is SaveSpotState.Success) {
            showSuccessOverlay = true
        }
    }

    NewSpotContent(
        uiState = uiState,
        onTakePhoto = {
            val file = createCaptureFile(context, "photo", ".jpg")
            pendingCameraFile = file
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            takePictureLauncher.launch(uri)
        },
        onPickFromGallery = {
            pickImagesLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        onPickAudio = { pickAudioLauncher.launch("audio/*") },
        onPickVideo = {
            pickVideoLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly),
            )
        },
        onRemoveMedia = viewModel::removeMedia,
        onBrandSelected = viewModel::onBrandSelected,
        onCategorySelected = viewModel::onCategorySelected,
        onModelChange = viewModel::onModelChange,
        onYearChange = viewModel::onYearChange,
        onPriceChange = viewModel::onPriceChange,
        onNotesChange = viewModel::onNotesChange,
        onOpenMapPicker = { showMapPicker = true },
        onSave = viewModel::saveSpot,
        onNavigateBack = onNavigateBack,
    )

    if (showMapPicker) {
        MapPickerDialog(
            initialLatitude = uiState.location?.first,
            initialLongitude = uiState.location?.second,
            onConfirm = { lat, lon ->
                viewModel.onLocationSelected(lat, lon)
                showMapPicker = false
            },
            onDismiss = { showMapPicker = false },
        )
    }

    if (showSuccessOverlay) {
        SaveSuccessOverlay(
            onComplete = {
                showSuccessOverlay = false
                viewModel.consumeSaveResult()
                onNavigateBack()
            },
        )
    }
}

private fun createCaptureFile(context: Context, prefix: String, suffix: String): File {
    val dir = File(context.cacheDir, "captures").apply { mkdirs() }
    return File(dir, "$prefix-${System.currentTimeMillis()}$suffix").apply { createNewFile() }
}

/** Copies a content:// URI into the app cache so we get an absolute file path. */
private fun copyUriToCache(
    context: Context,
    uri: Uri,
    prefix: String,
    suffix: String,
): String? = runCatching {
    val file = createCaptureFile(context, prefix, suffix)
    context.contentResolver.openInputStream(uri)?.use { input ->
        file.outputStream().use { input.copyTo(it) }
    } ?: return@runCatching null
    file.absolutePath
}.getOrNull()

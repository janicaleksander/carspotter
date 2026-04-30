package com.example.carspotter.ui.garage

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carspotter.repository.MediaDownloadTarget
import com.example.carspotter.viewmodels.GarageDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun GarageDetailScreen(
    viewModel: GarageDetailViewModel,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFavourite by viewModel.isFavourite.collectAsStateWithLifecycle()

    GarageDetailContent(
        uiState = uiState,
        isFavourite = isFavourite,
        onToggleFavourite = viewModel::toggleFavourite,
        onDownloadMedia = {
            scope.launch {
                val targets = viewModel.buildMediaDownloadTargets()
                val started = enqueueDownloads(context, targets)
                val message = when {
                    started == 0 -> "No media available to download"
                    started == 1 -> "Started downloading 1 file"
                    else -> "Started downloading $started files"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        },
        onRemoveFromGarage = viewModel::removeFromGarage,
        onNavigateBack = onNavigateBack,
        onDeleted = onNavigateBack,
    )
}

private fun enqueueDownloads(
    context: Context,
    targets: List<MediaDownloadTarget>,
): Int {
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        ?: return 0

    targets.forEach { target ->
        val request = DownloadManager.Request(Uri.parse(target.url))
            .setMimeType(target.mimeType)
            .setTitle(target.fileName)
            .setDescription("Downloading car media")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                target.fileName,
            )
        manager.enqueue(request)
    }

    return targets.size
}

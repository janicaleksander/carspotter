package com.example.carspotter.ui.garage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carspotter.models.MediaTypeEnum
import com.example.carspotter.ui.components.AudioPlayer
import com.example.carspotter.ui.components.CarInfoGrid
import com.example.carspotter.ui.components.Carousel
import com.example.carspotter.ui.components.CarouselItem
import com.example.carspotter.ui.components.MapPlaceholder
import com.example.carspotter.ui.components.TabHeader
import com.example.carspotter.ui.theme.CarRed
import com.example.carspotter.viewmodels.GarageCarDetails
import com.example.carspotter.viewmodels.GarageDetailState
import java.text.NumberFormat
import java.util.Locale

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageDetailContent(
    uiState: GarageDetailState,
    isFavourite: Boolean,
    onToggleFavourite: () -> Unit,
    onRemoveFromGarage: () -> Unit,
    onNavigateBack: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onDeleted()
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TabHeader(
                title = "${uiState.details.brandName} ${uiState.details.model}".trim(),
                onNavigateBack = onNavigateBack,
                onFavouriteClick = onToggleFavourite,
                isFavourite = isFavourite,
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier,
    ) { paddingValues ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val details = uiState.details

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item(key = "media_carousel") {
                val mediaItems = details.allMediaURLs.mapNotNull { media ->
                    when (media.type) {
                        MediaTypeEnum.PHOTO -> CarouselItem.Image(media.filePath)
                        MediaTypeEnum.VIDEO -> CarouselItem.Video(media.filePath)
                        else -> null
                    }
                }
                if (mediaItems.isNotEmpty()) {
                    Carousel(
                        items = mediaItems,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item(key = "description") {
                DescriptionSection(
                    details = details,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            item(key = "audio_player") {
                AudioPlayer(
                    url = details.allMediaURLs
                        .firstOrNull { it.type == MediaTypeEnum.AUDIO }?.filePath,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }

            item(key = "map") {
                MapPlaceholder(
                    latitude = details.location?.latitude,
                    longitude = details.location?.longitude,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }

            item(key = "remove_action") {
                RemoveFromGarageButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmRemoveDialog(
            carLabel = "${uiState.details.brandName} ${uiState.details.model}".trim(),
            onConfirm = {
                showDeleteDialog = false
                onRemoveFromGarage()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

// ─── Description ────────────────────────────────────────────────────────────────

@Composable
private fun DescriptionSection(
    details: GarageCarDetails,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "${details.brandName} ${details.model}".trim(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = formatPrice(details.price),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = CarRed,
        )

        if (details.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = details.notes,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        CarInfoGrid(
            items = listOf(
                "CATEGORY" to details.category.uppercase(),
                "YEAR" to details.year.toString(),
            ),
        )
    }
}

// ─── Remove button + confirm dialog ─────────────────────────────────────────────

@Composable
private fun RemoveFromGarageButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = CarRed),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Remove from garage",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ConfirmRemoveDialog(
    carLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Remove from garage?",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = if (carLabel.isBlank()) "This car will be removed from your garage."
                else "Are you sure you want to remove $carLabel from your garage?",
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = CarRed),
            ) {
                Text(text = "Remove", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cancel") }
        },
    )
}

private fun formatPrice(price: Double): String {
    if (price <= 0) return ""
    val formatter = NumberFormat.getIntegerInstance(Locale.GERMAN)
    return "${formatter.format(price.toLong())}$"
}

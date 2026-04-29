package com.example.carspotter.ui.new_spot

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.carspotter.models.MediaTypeEnum
import com.example.carspotter.ui.components.DropDown
import com.example.carspotter.ui.components.TabHeader
import com.example.carspotter.ui.theme.CarRed
import com.example.carspotter.viewmodels.NewSpotUiState
import com.example.carspotter.viewmodels.PickedMedia
import com.example.carspotter.viewmodels.SaveSpotState

@Composable
fun NewSpotContent(
    uiState: NewSpotUiState,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
    onPickAudio: () -> Unit,
    onPickVideo: () -> Unit,
    onRemoveMedia: (String) -> Unit,
    onBrandSelected: (String?) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onModelChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onOpenMapPicker: () -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = { TabHeader(title = "ADD NEW SPOT", onNavigateBack = onNavigateBack) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item("media") {
                MediaSection(
                    media = uiState.media,
                    error = uiState.errors.media,
                    onTakePhoto = onTakePhoto,
                    onPickFromGallery = onPickFromGallery,
                    onPickAudio = onPickAudio,
                    onPickVideo = onPickVideo,
                    onRemoveMedia = onRemoveMedia,
                )
            }
            item("details") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("VEHICLE DETAILS", fontWeight = FontWeight.ExtraBold)

                    LabeledField("Brand", uiState.errors.brand) {
                        DropDown(
                            label = uiState.brands.firstOrNull { it.id == uiState.brandId }?.name
                                ?: "Select brand",
                            isSelected = uiState.brandId != null,
                            options = uiState.brands.map { it.id as String? to it.name },
                            onOptionSelected = onBrandSelected,
                        )
                    }

                    LabeledField("Model", uiState.errors.model) {
                        Input(uiState.model, onModelChange, "e.g. 911 GT3", error = uiState.errors.model)
                    }

                    LabeledField("Category", uiState.errors.category) {
                        DropDown(
                            label = uiState.categories.firstOrNull { it.id == uiState.categoryId }?.name
                                ?: "Select category",
                            isSelected = uiState.categoryId != null,
                            options = uiState.categories.map { it.id as String? to it.name },
                            onOptionSelected = onCategorySelected,
                        )
                    }

                    LabeledField("Year", uiState.errors.year) {
                        Input(uiState.year, onYearChange, "e.g. 2023", KeyboardType.Number, error = uiState.errors.year)
                    }

                    LabeledField("Price", uiState.errors.price) {
                        Input(uiState.price, onPriceChange, "e.g. 2,000.00", KeyboardType.Decimal, error = uiState.errors.price)
                    }
                }
            }
            item("location") {
                LabeledField("Location", uiState.errors.location) {
                    LocationRow(
                        label = uiState.locationLabel,
                        isError = uiState.errors.location != null,
                        onClick = onOpenMapPicker,
                    )
                }
            }
            item("notes") {
                LabeledField("Spotter Notes", error = uiState.errors.notes) {
                    Input(
                        value = uiState.notes,
                        onValueChange = onNotesChange,
                        placeholder = "Mention specific modifications, rarity, or how you found it…",
                        singleLine = false,
                        error = uiState.errors.notes,
                    )
                }
            }
            item("save") {
                SaveSection(
                    isOnline = uiState.isOnline,
                    saveState = uiState.saveState,
                    hasErrors = uiState.errors.hasAny,
                    onSave = onSave,
                )
            }
        }
    }
}

// ─── Media ──────────────────────────────────────────────────────────────────────

@Composable
private fun MediaSection(
    media: List<PickedMedia>,
    error: String?,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
    onPickAudio: () -> Unit,
    onPickVideo: () -> Unit,
    onRemoveMedia: (String) -> Unit,
) {
    val hasAudio = media.any { it.type == MediaTypeEnum.AUDIO }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("MEDIA & FILES", fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            // Hint by default (subdued); flips to a red error only when [error] is set.
            Text(
                text = error ?: "Required: 1 photo",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (error != null) FontWeight.Bold else FontWeight.Medium,
                color = if (error != null) CarRed else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MediaTile("Photo", Icons.Default.PhotoCamera, onTakePhoto, Modifier.weight(1f))
            MediaTile("Gallery", Icons.Default.PhotoLibrary, onPickFromGallery, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MediaTile(
                label = if (hasAudio) "Replace Sound" else "Engine Sound",
                icon = Icons.Default.Mic,
                onClick = onPickAudio,
                modifier = Modifier.weight(1f),
            )
            MediaTile("Video", Icons.Default.Videocam, onPickVideo, Modifier.weight(1f))
        }
        if (media.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(media, key = { it.localPath }) { Thumbnail(it) { onRemoveMedia(it.localPath) } }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .4f),
        modifier = modifier.height(96.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, null, tint = CarRed, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Thumbnail(item: PickedMedia, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 130.dp, height = 110.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .4f)),
    ) {
        when (item.type) {
            MediaTypeEnum.PHOTO -> AsyncImage(
                model = item.localPath,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            MediaTypeEnum.VIDEO -> Icon(
                Icons.Default.Videocam, null,
                modifier = Modifier.size(36.dp).align(Alignment.Center),
            )
            MediaTypeEnum.AUDIO -> Icon(
                Icons.Default.Audiotrack, null,
                modifier = Modifier.size(36.dp).align(Alignment.Center),
            )
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(26.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = .6f)),
        ) {
            Icon(Icons.Default.Close, "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

// ─── Form input ─────────────────────────────────────────────────────────────────

/**
 * Form row with a label on top and an optional small red error caption below
 * the field. Errors are only set after the user attempts to save (handled in
 * the ViewModel).
 */
@Composable
private fun LabeledField(
    label: String,
    error: String?,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold)
        content()
        if (error != null) {
            Text(
                text = error,
                color = CarRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

@Composable
private fun Input(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    error: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        isError = error != null,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 4,
        maxLines = if (singleLine) 1 else 8,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .4f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .2f),
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationRow(
    label: String?,
    isError: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .4f),
        border = if (isError) BorderStroke(1.dp, CarRed) else null,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.LocationOn, null, tint = CarRed, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(label ?: "Tap to pick on map")
        }
    }
}

// ─── Save ───────────────────────────────────────────────────────────────────────

@Composable
private fun SaveSection(
    isOnline: Boolean,
    saveState: SaveSpotState,
    hasErrors: Boolean,
    onSave: () -> Unit,
) {
    // Inline field errors carry the same info, so suppress the duplicate
    // generic "fix the highlighted fields" message once they're on screen.
    val isFieldErrorRedundant = saveState is SaveSpotState.Error && hasErrors

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onSave,
            // Always clickable while not saving — pressing the button is what
            // reveals the inline field errors on the first attempt.
            enabled = saveState !is SaveSpotState.Saving,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CarRed),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            if (saveState is SaveSpotState.Saving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Save Spot", fontWeight = FontWeight.Bold)
            }
        }
        val errorText = when {
            !isOnline -> "You're offline. New spots can't be saved without internet."
            isFieldErrorRedundant -> null
            saveState is SaveSpotState.Error -> saveState.message
            else -> null
        }
        if (errorText != null) Text(errorText, color = CarRed)
    }
}

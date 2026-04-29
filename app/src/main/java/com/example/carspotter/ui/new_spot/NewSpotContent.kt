package com.example.carspotter.ui.new_spot

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
import androidx.compose.foundation.layout.imePadding
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
import com.example.carspotter.ui.components.AppDropdownField
import com.example.carspotter.ui.components.AppTextField
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
                .padding(padding)
                .imePadding(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
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
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text(
                        text = "VEHICLE DETAILS",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    AppDropdownField(
                        label = "Brand",
                        selectedLabel = uiState.brands.firstOrNull { it.id == uiState.brandId }?.name.orEmpty(),
                        options = uiState.brands.map { it.id as String? to it.name },
                        onOptionSelected = onBrandSelected,
                        placeholder = "Select brand",
                        errorText = uiState.errors.brand,
                    )

                    AppTextField(
                        label = "Model",
                        value = uiState.model,
                        onValueChange = onModelChange,
                        placeholder = "e.g. 911 GT3",
                        errorText = uiState.errors.model,
                    )

                    AppDropdownField(
                        label = "Category",
                        selectedLabel = uiState.categories.firstOrNull { it.id == uiState.categoryId }?.name.orEmpty(),
                        options = uiState.categories.map { it.id as String? to it.name },
                        onOptionSelected = onCategorySelected,
                        placeholder = "Select category",
                        errorText = uiState.errors.category,
                    )

                    AppTextField(
                        label = "Year",
                        value = uiState.year,
                        onValueChange = onYearChange,
                        placeholder = "e.g. 2023",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        errorText = uiState.errors.year,
                    )

                    AppTextField(
                        label = "Price",
                        value = uiState.price,
                        onValueChange = onPriceChange,
                        placeholder = "e.g. 2,000.00",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        errorText = uiState.errors.price,
                    )
                }
            }
            item("location") {
                AppTextField(
                    label = "Location",
                    value = uiState.locationLabel.orEmpty(),
                    onValueChange = {},
                    placeholder = "Tap to pick on map",
                    readOnly = true,
                    onClick = onOpenMapPicker,
                    errorText = uiState.errors.location,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = CarRed,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                )
            }
            item("notes") {
                AppTextField(
                    label = "Spotter Notes",
                    value = uiState.notes,
                    onValueChange = onNotesChange,
                    placeholder = "Mention specific modifications, rarity, or how you found it...",
                    singleLine = false,
                    errorText = uiState.errors.notes,
                )
            }
            item("save") {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
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
}

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

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "MEDIA & FILES",
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = error ?: "Required: 1 photo",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (error != null) FontWeight.Bold else FontWeight.Medium,
                color = if (error != null) CarRed else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MediaTile("Photo", Icons.Default.PhotoCamera, onTakePhoto, Modifier.weight(1f))
            MediaTile("Gallery", Icons.Default.PhotoLibrary, onPickFromGallery, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                items(media, key = { it.localPath }) { item ->
                    Thumbnail(item) { onRemoveMedia(item.localPath) }
                }
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
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier.height(104.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, null, tint = CarRed, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Thumbnail(item: PickedMedia, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 130.dp, height = 110.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        when (item.type) {
            MediaTypeEnum.PHOTO -> AsyncImage(
                model = item.localPath,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            MediaTypeEnum.VIDEO -> Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.Center),
            )
            MediaTypeEnum.AUDIO -> Icon(
                imageVector = Icons.Default.Audiotrack,
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.Center),
            )
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(26.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.6f)),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun SaveSection(
    isOnline: Boolean,
    saveState: SaveSpotState,
    hasErrors: Boolean,
    onSave: () -> Unit,
) {
    val isFieldErrorRedundant = saveState is SaveSpotState.Error && hasErrors

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onSave,
            enabled = isOnline && saveState !is SaveSpotState.Saving,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CarRed,
                disabledContainerColor = CarRed.copy(alpha = 0.45f),
                disabledContentColor = Color.White.copy(alpha = 0.75f),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
        ) {
            if (saveState is SaveSpotState.Saving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(text = "Save Spot", fontWeight = FontWeight.Bold)
            }
        }

        val errorText = when {
            !isOnline -> "You're offline. New spots can't be saved without internet."
            isFieldErrorRedundant -> null
            saveState is SaveSpotState.Error -> saveState.message
            else -> null
        }

        if (errorText != null) {
            Text(
                text = errorText,
                color = CarRed,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

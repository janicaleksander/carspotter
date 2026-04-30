package com.example.carspotter.ui.tops

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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carspotter.models.MediaTypeEnum
import com.example.carspotter.ui.components.AudioPlayer
import com.example.carspotter.ui.components.CarInfoGrid
import com.example.carspotter.ui.components.Carousel
import com.example.carspotter.ui.components.CarouselItem
import com.example.carspotter.ui.components.EmptyListHint
import com.example.carspotter.ui.components.TabHeader
import com.example.carspotter.ui.theme.CarRed
import com.example.carspotter.viewmodels.DetailTopCarState


@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopsDetailContent(
    uiState: DetailTopCarState,
    ifUserHasDream: Boolean,
    onAddToDream: (String) -> Unit,
    onRemoveFromDream: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TabHeader(
                title = if (uiState.isLoading) "TOP CAR"
                        else "TOP CAR - ${uiState.details.brandName} ${uiState.details.model}",
                onNavigateBack = onNavigateBack,
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item(key = "media_carousel") {
                val mediaItems = uiState.details.allMediaURLs.mapNotNull {
                    when (it.type) {
                        MediaTypeEnum.PHOTO -> CarouselItem.Image(it.filePath)
                        MediaTypeEnum.VIDEO -> CarouselItem.Video(it.filePath)
                        else -> null
                    }
                }
                if (mediaItems.isNotEmpty()) {
                    Carousel(
                        items = mediaItems,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    EmptyListHint(
                        primary = "No photos or videos",
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }

            item(key = "description") {
                DescriptionSection(
                    brandName = uiState.details.brandName,
                    model = uiState.details.model,
                    description = uiState.details.description,
                    category = uiState.details.category,
                    year = uiState.details.year.toString(),
                    power = "${uiState.details.powerHP}HP",
                    acceleration = uiState.details.acceleration.toString(),
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            item(key = "audio_player") {
                AudioPlayer(
                    url = uiState.details.allMediaURLs
                        .firstOrNull { it.type == MediaTypeEnum.AUDIO }?.filePath,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }

            item(key = "dream_actions") {
                DreamActions(
                    isDream = ifUserHasDream,
                    onAddToDream = { onAddToDream(uiState.details.carId) },
                    onRemoveFromDream = { onRemoveFromDream(uiState.details.carId) },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }
}


@Composable
private fun DescriptionSection(
    brandName: String,
    model: String,
    description: String,
    category: String,
    year: String,
    power: String,
    acceleration: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "$brandName $model",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Description",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(20.dp))

        CarInfoGrid(
            items = listOf(
                "CATEGORY" to category,
                "YEAR" to year,
                "POWER" to power,
                "0-100" to acceleration,
            ),
        )
    }
}


@Composable
private fun DreamActions(
    isDream: Boolean,
    onAddToDream: () -> Unit,
    onRemoveFromDream: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        if (isDream) {
            OutlinedButton(
                onClick = onRemoveFromDream,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CarRed),
                modifier = Modifier
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
                    text = "Remove from dream",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        } else {
            Button(
                onClick = onAddToDream,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add to your dream",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

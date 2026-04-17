package com.example.carspotter.ui.tops

import android.graphics.drawable.Icon
import android.media.browse.MediaBrowser
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.carspotter.models.MediaTypeEnum
import com.example.carspotter.ui.components.Carousel
import com.example.carspotter.ui.components.CarouselItem
import com.example.carspotter.ui.theme.CarRed
import com.example.carspotter.viewmodels.DetailTopCarState
import okhttp3.MediaType

@Composable
fun TopsDetailContent(
    uiState: DetailTopCarState,
    ifUserHasDream: Boolean,
    onAddToDream: (String) -> Unit,
    onRemoveFromDream: (String) -> Unit
) {
    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }


    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item(key = "header") {
            Header(
                "TOP CAR" +uiState.details.brandName +" " + uiState.details.model
            )
        }

        item(key = "media_carousel") {
            val mediaItems = uiState.details.allMediaURLs.map {
                when (it.type) {
                    MediaTypeEnum.PHOTO -> CarouselItem.Image(it.filePath)
                    MediaTypeEnum.VIDEO -> CarouselItem.Video(it.filePath)
                    else -> null
                }
            }.filterNotNull()
            if (mediaItems.isNotEmpty()) {
                Carousel(items = mediaItems)
            }
        }

        item(key = "description") {
            Description(
                uiState.details.brandName,
                uiState.details.model,
                uiState.details.description,
                uiState.details.category,
                uiState.details.year.toString(),
                uiState.details.powerHP.toString(),
                uiState.details.acceleration.toString()
            )
        }

        item(key = "play_button") {
            AudioPlayer(
                url = uiState.details.allMediaURLs.firstOrNull { it.type == MediaTypeEnum.AUDIO }?.filePath
            )
        }

        item(key = "is_dream") {
            if(ifUserHasDream){
                addRemoveDreamCar(
                    text = "REMOVE FROM DREAM GARAGE",
                    onClick = { onRemoveFromDream(uiState.details.carId) },
                    icon = Icons.Default.DirectionsCar
                )

            }else{
                addRemoveDreamCar(
                    text = "ADD TO DREAM GARAGE",
                    onClick = { onAddToDream(uiState.details.carId) },
                    icon = Icons.Default.DirectionsCar
                )

            }
        }
    }
}

@Composable
fun Header(
    text:String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp, bottom = 4.dp),
    )
}



@Composable
fun Description(
        brand:String,
        model:String,
        description:String,
        category:String,
        year:String,
        power: String,
        acceleration:String
){
    Text(
        text = "$brand $model",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Left,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp, bottom = 4.dp),
    )
    Text(
        text = "Description:",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Left,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp, bottom = 4.dp),
    )

    Text(
        text = description,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Left,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp, bottom = 4.dp),
    )

    InfoGrid2x2(
        category,
        year,
        power,
        acceleration
    )


}

@Composable
fun InfoChip(
    label: String,
    value: String,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
@Composable
fun InfoGrid2x2(
    category:String,
    year:String,
    power: String,
    acceleration:String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoChip("CATEGORY", category)
            InfoChip("YEAR", year)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoChip("POWER", power)
            InfoChip("0-100", acceleration)
        }
    }
}


@Composable
fun playSoundButton(
    audioURLString:String?, //ONLY ONE PER CAR
    onClick: () -> Unit
){
    Button(
        onClick = onClick,
        enabled = audioURLString != null, // if we have a url audio
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CarRed,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
    ){
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = Color.White,
        )
      Text(
          text = "PLAY ENGINE SOUND"
      )
    }
}


@Composable
fun addRemoveDreamCar(
    text:String,
    onClick: ()-> Unit,
    icon: ImageVector
){
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CarRed,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
    ){
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = Color.White,
        )
      Text(
          text = text
      )
    }
}

@Composable
fun AudioPlayer(url:String?){
    if (url == null) return

    val ctx = LocalContext.current


    val player = remember {
        ExoPlayer.Builder(ctx).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    var isPlaying by remember { mutableStateOf(false) }
    playSoundButton(
        url,
        onClick = {
            if (isPlaying) player.pause() else player.play()
            isPlaying = !isPlaying
        }
    )
}
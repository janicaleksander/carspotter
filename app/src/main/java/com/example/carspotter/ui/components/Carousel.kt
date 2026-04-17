package com.example.carspotter.ui.components

import android.media.browse.MediaBrowser
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage

sealed class CarouselItem{
    data class Image(val url: String): CarouselItem()
    data class Video(val url: String): CarouselItem()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Carousel(
    items: List<CarouselItem>
){
    HorizontalUncontainedCarousel(
        state = rememberCarouselState {items.size },
        itemWidth = 300.dp

    ) {index ->
        when (val item = items[index]) {
            is CarouselItem.Image -> {
                ZoomableImage(url = item.url)
            }
            is CarouselItem.Video -> {
                VideoPlayer(url = item.url)
            }
        }
    }

}

//and this has to be really 8K photo viewer with pinch to zoom and pan functionality, so we need to use graphicsLayer and pointerInput to achieve that
@Composable
fun ZoomableImage(
    url: String
){
    var scale by remember { mutableFloatStateOf(1f) }
    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    scale *= zoom
                    scale = scale.coerceIn(1f, 5f) // Limit zoom between 1x and 5x
                }
            }
    )
}

@Composable
fun VideoPlayer(
    url: String
){
    val ctx = LocalContext.current

    val player = remember {
        ExoPlayer.Builder(ctx).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
        }
    }

    AndroidView(
        factory = { PlayerView(ctx).apply { this.player = player } },
        modifier = Modifier.fillMaxSize()
    )
}
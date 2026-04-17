package com.example.carspotter.ui.components

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState

import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState

sealed class CarouselItem {
    data class Image(val url: String) : CarouselItem()
    data class Video(val url: String) : CarouselItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
@Composable
fun Carousel(
    items: List<CarouselItem>,
    modifier: Modifier = Modifier,
) {
    HorizontalUncontainedCarousel(
        state = rememberCarouselState { items.size },
        itemWidth = 300.dp,
        modifier = modifier,
    ) { index ->
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

//and this has to be really 8K photo viewer with pinch to zoom and pan functionality,
// so we need to use graphicsLayer and pointerInput to achieve that.
// Coil's ImageRequest with Size.ORIGINAL ensures we decode the full resolution bitmap.
@Composable
private fun ZoomableImage(
    url: String,
    modifier: Modifier = Modifier,
) {
    ZoomableAsyncImage(
        model = url,
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        state = rememberZoomableImageState(
            rememberZoomableState(
                zoomSpec = ZoomSpec(maxZoomFactor = 5f)
            )
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
@Composable
private fun VideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current

    val player = remember {
        ExoPlayer.Builder(ctx)
            .setRenderersFactory(
                DefaultRenderersFactory(ctx).apply {
                    setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
                }
            )
            .build().apply {
                setMediaItem(MediaItem.fromUri(url))
                prepare()
            }
    }
    // Release the player when this composable leaves the composition to avoid memory leaks
    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    AndroidView(
        factory = { PlayerView(ctx).apply { this.player = player } },
        modifier = modifier.fillMaxSize(),
    )
}
package com.example.carspotter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState

sealed class CarouselItem {
    data class Image(val url: String) : CarouselItem()
    data class Video(val url: String) : CarouselItem()
}

// ─── Single-page carousel (HorizontalPager) ────────────────────────────────────
// Shows one full-width item at a time with swipe-to-navigate + page indicator dots.
// This is the idiomatic Compose approach for a "one item visible" gallery.

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun Carousel(
    items: List<CarouselItem>,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { items.size })

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
        ) { page ->
            when (val item = items[page]) {
                is CarouselItem.Image -> {
                    ZoomableImage(url = item.url)
                }
                is CarouselItem.Video -> {
                    VideoPlayer(url = item.url)
                }
            }
        }

        // Page indicator dots — only shown when there are multiple items
        if (items.size > 1) {
            PageIndicator(
                pageCount = items.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }
    }
}

// ─── Page indicator dots ────────────────────────────────────────────────────────

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (isSelected) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                    ),
            )
        }
    }
}

// ─── 8K zoomable image ──────────────────────────────────────────────────────────
// Uses telephoto's ZoomableAsyncImage which supports subsampling:
// - Only the visible tiles are decoded at the current zoom level
// - Handles 8K (7680×4320) without loading ~132 MB into RAM
// - Smooth pinch-to-zoom and pan with gesture support
// - Max zoom factor = 8× for full 8K pixel-peeping on a 1080p phone

@Composable
private fun ZoomableImage(
    url: String,
    modifier: Modifier = Modifier,
) {
    ZoomableAsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize(),
        state = rememberZoomableImageState(
            rememberZoomableState(
                zoomSpec = ZoomSpec(maxZoomFactor = 8f),
            ),
        ),
    )
}

// ─── Video player ───────────────────────────────────────────────────────────────

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
                },
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
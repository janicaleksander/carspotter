package com.example.carspotter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.request.ImageRequest
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
                is CarouselItem.Image -> ZoomableImage(url = item.url)
                is CarouselItem.Video -> VideoPlayer(url = item.url)
            }
        }

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
// telephoto's ZoomableAsyncImage handles 8K (7680×4320) idiomatically:
// - Uses SubSamplingImage under the hood: only visible tiles are decoded
// - Never loads the full ~132 MB bitmap into RAM
// - Pinch-to-zoom + pan gestures out of the box
// Coil's ImageRequest is built explicitly so the source bitmap isn't downsampled
// by Coil before telephoto gets a chance to stream tiles at zoom level.

@Composable
private fun ZoomableImage(
    url: String,
    modifier: Modifier = Modifier,
) {
    var fullscreen by remember { mutableStateOf(false) }
    val ctx = LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
        ZoomableAsyncImage(
            model = ImageRequest.Builder(ctx)
                .data(url)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            state = rememberZoomableImageState(
                rememberZoomableState(
                    zoomSpec = ZoomSpec(maxZoomFactor = 8f),
                ),
            ),
        )
        FullscreenButton(
            onClick = { fullscreen = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
        )
    }

    if (fullscreen) {
        FullscreenImageDialog(url = url, onDismiss = { fullscreen = false })
    }
}

// ─── Fullscreen image dialog ────────────────────────────────────────────────────
// Uses a platform-width-less Dialog so the image takes the entire screen.
// ContentScale.Fit + higher maxZoomFactor lets the user pixel-peek on 8K content.

@Composable
private fun FullscreenImageDialog(
    url: String,
    onDismiss: () -> Unit,
) {
    val ctx = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            ZoomableAsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                state = rememberZoomableImageState(
                    rememberZoomableState(
                        zoomSpec = ZoomSpec(maxZoomFactor = 10f),
                    ),
                ),
            )
            CloseButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
            )
        }
    }
}

// ─── Video player ───────────────────────────────────────────────────────────────
// Keyed on `url` so switching pages reinitialises the player for a new stream.
// A single ExoPlayer instance is reused between the embedded view and the
// fullscreen dialog by moving it off the old PlayerView (onRelease → player = null)
// and attaching it to the new PlayerView that the dialog creates.

@UnstableApi
@Composable
private fun VideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current

    val player = remember(url) {
        ExoPlayer.Builder(ctx)
            .setRenderersFactory(
                DefaultRenderersFactory(ctx).apply {
                    setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
                },
            )
            .build().apply {
                setMediaItem(MediaItem.fromUri(url))
                prepare()
                playWhenReady = false
            }
    }

    var playbackError by remember(url) { mutableStateOf<PlaybackException?>(null) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                playbackError = error
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    var fullscreen by remember { mutableStateOf(false) }

    val error = playbackError
    if (error != null) {
        VideoErrorView(error = error, modifier = modifier.fillMaxSize())
        return
    }

    if (!fullscreen) {
        Box(modifier = modifier.fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = player
                        useController = true
                    }
                },
                modifier = Modifier.fillMaxSize(),
                onRelease = { it.player = null },
            )
            FullscreenButton(
                onClick = { fullscreen = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            )
        }
    } else {
        // Reserve the space while the embedded view is detached so the
        // surrounding layout doesn't collapse.
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
        )
        FullscreenVideoDialog(
            player = player,
            onDismiss = { fullscreen = false },
        )
    }
}

@UnstableApi
@Composable
private fun FullscreenVideoDialog(
    player: ExoPlayer,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = player
                        useController = true
                    }
                },
                modifier = Modifier.fillMaxSize(),
                onRelease = { it.player = null },
            )
            CloseButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
            )
        }
    }
}

// ─── Video error view ───────────────────────────────────────────────────────────
// Rendered instead of PlayerView when ExoPlayer reports a fatal playback error.
// Codec-related error codes get a dedicated message; anything else is treated
// as a generic playback failure.

@Composable
private fun VideoErrorView(
    error: PlaybackException,
    modifier: Modifier = Modifier,
) {
    val message = when (error.errorCode) {
        PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
        PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED,
        PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED,
        PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES,
        -> "Kodek wideo nie jest obsługiwany przez to urządzenie"
        else -> "Nie można odtworzyć filmu"
    }

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Icon(
                imageVector = Icons.Default.VideocamOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

// ─── Overlay buttons ────────────────────────────────────────────────────────────

@Composable
private fun FullscreenButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(36.dp)
            .background(Color.Black.copy(alpha = 0.4f), CircleShape),
    ) {
        Icon(
            imageVector = Icons.Default.Fullscreen,
            contentDescription = "Fullscreen",
            tint = Color.White,
        )
    }
}

@Composable
private fun CloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .background(Color.Black.copy(alpha = 0.5f), CircleShape),
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = Color.White,
        )
    }
}

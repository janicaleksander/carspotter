package com.example.carspotter.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.carspotter.ui.theme.CarRed
import com.example.carspotter.services.NominatimReverseGeocoder
import java.util.Locale
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker


@Composable
fun MapPickerDialog(
    initialLatitude: Double?,
    initialLongitude: Double?,
    onConfirm: (Double, Double) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val userAgent = remember(context) { context.packageName }

    DisposableEffect(Unit) {
        Configuration.getInstance().load(
            context.applicationContext,
            context.applicationContext
                .getSharedPreferences("osmdroid", Context.MODE_PRIVATE),
        )
        Configuration.getInstance().userAgentValue = userAgent
        onDispose { }
    }

    var picked by remember {
        mutableStateOf(
            if (initialLatitude != null && initialLongitude != null) {
                GeoPoint(initialLatitude, initialLongitude)
            } else null,
        )
    }
    var address by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(picked) {
        val p = picked ?: return@LaunchedEffect
        address = NominatimReverseGeocoder.resolve(p.latitude, p.longitude, userAgent)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                MapPickerHeader(onClose = onDismiss)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            MapView(ctx).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                setHorizontalMapRepetitionEnabled(false)
                                setVerticalMapRepetitionEnabled(false)
                                zoomController.setVisibility(
                                    org.osmdroid.views.CustomZoomButtonsController
                                        .Visibility.NEVER,
                                )

                                val center = picked
                                    ?: GeoPoint(52.2297, 21.0122) // Warsaw fallback
                                controller.setZoom(if (picked != null) 14.0 else 5.0)
                                controller.setCenter(center)

                                overlays.add(CopyrightOverlay(ctx))

                                overlays.add(
                                    MapEventsOverlay(object : MapEventsReceiver {
                                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                            if (p != null) picked = p
                                            return true
                                        }
                                        override fun longPressHelper(p: GeoPoint?): Boolean = false
                                    }),
                                )
                            }
                        },
                        update = { map ->
                            val markerOverlays = map.overlays
                                .filterIsInstance<Marker>()
                            markerOverlays.forEach { map.overlays.remove(it) }

                            val current = picked
                            if (current != null) {
                                val marker = Marker(map).apply {
                                    position = current
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    isDraggable = true
                                    setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                                        override fun onMarkerDrag(marker: Marker) {}
                                        override fun onMarkerDragEnd(marker: Marker) {
                                            picked = marker.position
                                        }
                                        override fun onMarkerDragStart(marker: Marker) {}
                                    })
                                }
                                map.overlays.add(marker)
                            }
                            map.invalidate()
                        },
                        onRelease = { map ->
                            map.overlays.clear()
                            map.onDetach()
                        },
                    )
                }

                MapPickerFooter(
                    address = address,
                    picked = picked,
                    onConfirm = {
                        val p = picked ?: return@MapPickerFooter
                        onConfirm(p.latitude, p.longitude)
                    },
                )
            }
        }
    }
}

@Composable
private fun MapPickerHeader(onClose: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
            Text(
                text = "Pick location",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
            )
        }
    }
}

@Composable
private fun MapPickerFooter(
    address: String?,
    picked: GeoPoint?,
    onConfirm: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = CarRed,
                    modifier = Modifier.size(20.dp),
                )
                Box(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = address
                            ?: picked?.let { "Resolving address…" }
                            ?: "Tap on the map to drop a marker",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (picked != null) {
                        Text(
                            text = String.format(
                                Locale.US,
                                "%.5f, %.5f",
                                picked.latitude,
                                picked.longitude,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Button(
                onClick = onConfirm,
                enabled = picked != null,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CarRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                Text(
                    text = "Use this location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

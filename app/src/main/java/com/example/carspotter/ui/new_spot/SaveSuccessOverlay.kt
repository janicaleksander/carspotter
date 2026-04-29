package com.example.carspotter.ui.new_spot

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carspotter.ui.theme.CarRed
import com.example.carspotter.ui.theme.Neutral10
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun SaveSuccessOverlay(onComplete: () -> Unit) {
    val curtain   = remember { Animatable(0f) }
    val flagIn    = remember { Animatable(0f) }
    val titleScale = remember { Animatable(0f) }
    val streaks   = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1 – curtain in
        curtain.animateTo(1f, tween(280))

        // 2 – flag, title, streaks in parallel
        coroutineScope {
            launch {
                flagIn.animateTo(
                    1f,
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessLow,
                    ),
                )
            }
            launch {
                delay(150)
                titleScale.animateTo(
                    1f,
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessLow,
                    ),
                )
            }
            launch { streaks.animateTo(1f, tween(700, easing = LinearEasing)) }
        }

        // 3 – hold, then out
        delay(550)
        curtain.animateTo(0f, tween(250))
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(curtain.value)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Neutral10.copy(alpha = 0.94f),
                        Color.Black.copy(alpha = 0.98f),
                    ),
                ),
            )
            .pointerInput(Unit) {},
        contentAlignment = Alignment.Center,
    ) {
        SpeedStreaks(progress = streaks.value, modifier = Modifier.fillMaxSize())

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CheckeredFlag(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        alpha      = flagIn.value
                        scaleX     = flagIn.value
                        scaleY     = flagIn.value
                        rotationZ  = (1f - flagIn.value) * -25f
                    },
            )
            Spacer(Modifier.height(28.dp))
            Text(
                text       = "SPOT SAVED!",
                color      = Color.White,
                fontSize   = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                modifier   = Modifier.graphicsLayer {
                    scaleX = titleScale.value
                    scaleY = titleScale.value
                    alpha  = titleScale.value.coerceIn(0f, 1f)
                },
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text          = "RACING TO YOUR GARAGE",
                color         = Color.White.copy(alpha = 0.55f),
                fontSize      = 12.sp,
                letterSpacing = 3.sp,
                modifier      = Modifier.alpha(titleScale.value.coerceIn(0f, 1f)),
            )
        }
    }
}

@Composable
private fun CheckeredFlag(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w     = size.width
        val h     = size.height
        val flagW = w * 0.72f
        val flagH = h * 0.50f
        val flagX = (w - flagW) / 2f
        val flagY = (h - flagH) / 2f
        val cols  = 8
        val rows  = 5
        val cellW = flagW / cols
        val cellH = flagH / rows

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                drawRect(
                    color   = if ((r + c) % 2 == 0) Color.White else Color.Black,
                    topLeft = Offset(flagX + c * cellW, flagY + r * cellH),
                    size    = Size(cellW, cellH),
                )
            }
        }

        drawLine(
            color       = Color.White.copy(alpha = 0.85f),
            start       = Offset(flagX - 6.dp.toPx(), flagY - 6.dp.toPx()),
            end         = Offset(flagX - 6.dp.toPx(), flagY + flagH + 28.dp.toPx()),
            strokeWidth = 4.dp.toPx(),
            cap         = StrokeCap.Round,
        )
    }
}

@Composable
private fun SpeedStreaks(progress: Float, modifier: Modifier = Modifier) {
    if (progress <= 0f) return
    Canvas(modifier = modifier) {
        val w      = size.width
        val h      = size.height
        val len    = w * 0.32f
        val travel = w + len * 2

        repeat(14) { i ->
            val y     = ((i * 8973L) % 100L) / 100f * h
            val phase = (progress + i / 14f) % 1f
            val alpha = (1f - abs(phase - 0.5f) * 2f) * 0.55f
            drawLine(
                color       = CarRed.copy(alpha = alpha),
                start       = Offset(-len + travel * phase, y),
                end         = Offset(-len + travel * phase + len, y),
                strokeWidth = 3.dp.toPx(),
                cap         = StrokeCap.Round,
            )
        }
    }
}
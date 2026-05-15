package com.example.carspotter.ui.new_spot

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carspotter.ui.theme.CarRed
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SaveSuccessOverlay(onComplete: () -> Unit) {
    val curtain    = remember { Animatable(0f) }
    val needleAngle = remember { Animatable(NEEDLE_MIN) }
    val speedKmh   = remember { Animatable(0f) }
    val titleScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1 – curtain in
        curtain.animateTo(1f, tween(280))

        // 2 – needle sweep + counter
        coroutineScope { // this block is not going to finish until every launch finish inside
            launch {
                needleAngle.animateTo(NEEDLE_MAX, tween(900, easing = FastOutSlowInEasing))
                needleAngle.animateTo(
                    NEEDLE_MAX - 8f,
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessMedium,
                    ),
                )
            }
            launch {
                speedKmh.animateTo(MAX_KMH.toFloat(), tween(900, easing = FastOutSlowInEasing))
            }
        }

        // 3 – title
        coroutineScope {
            launch {
                titleScale.animateTo(
                    1f,
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessLow,
                    ),
                )
            }
        }

        // 4 – hold, then finish (no curtain fade-out — it revealed the form underneath)
        delay(550)
        onComplete() // callback what to do after animation finishes
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(curtain.value)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.97f),
                        Color(0xFFF0F0F0).copy(alpha = 0.99f),
                    ),
                ),
            )
            .pointerInput(Unit) {},// block all UI interactions under the curtain
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.offset(y = (-14).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Speedometer(
                needleAngle = needleAngle.value,
                speed       = speedKmh.value.toInt(),
                modifier    = Modifier.size(240.dp),
            )
            Spacer(Modifier.height(28.dp))
            Text(
                text          = "SPOT SAVED!",
                color         = CarRed,
                fontSize      = 32.sp,
                fontWeight    = FontWeight.Black,
                letterSpacing = 4.sp,
                modifier      = Modifier.graphicsLayer {
                    scaleX = titleScale.value
                    scaleY = titleScale.value
                    alpha  = titleScale.value.coerceIn(0f, 1f)
                },
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text          = "RACING TO YOUR GARAGE",
                color         = CarRed.copy(alpha = 0.55f),
                fontSize      = 12.sp,
                letterSpacing = 3.sp,
                modifier      = Modifier.alpha(titleScale.value.coerceIn(0f, 1f)),
            )
        }
    }
}

@Composable
private fun Speedometer(
    needleAngle: Float,
    speed: Int,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx       = size.width / 2f
            val cy       = size.height / 2f
            val radius   = size.minDimension / 2f - 18.dp.toPx()
            val arcStroke = 10.dp.toPx()

            drawArc(
                color      = Color(0xFFCCCCCC),
                startAngle = NEEDLE_MIN,
                sweepAngle = NEEDLE_MAX - NEEDLE_MIN,
                useCenter  = false,
                style      = Stroke(width = arcStroke, cap = StrokeCap.Round),
                topLeft    = Offset(cx - radius, cy - radius),
                size       = Size(radius * 2, radius * 2),
            )

            val sweep = (needleAngle - NEEDLE_MIN).coerceIn(0f, NEEDLE_MAX - NEEDLE_MIN)
            drawArc(
                color      = CarRed,
                startAngle = NEEDLE_MIN,
                sweepAngle = sweep,
                useCenter  = false,
                style      = Stroke(width = arcStroke, cap = StrokeCap.Round),
                topLeft    = Offset(cx - radius, cy - radius),
                size       = Size(radius * 2, radius * 2),
            )

            // Ticki
            val ticks      = 11
            val tickStep   = (NEEDLE_MAX - NEEDLE_MIN) / (ticks - 1)
            val innerRadius = radius - 14.dp.toPx()
            for (i in 0 until ticks) {
                val angleRad = (NEEDLE_MIN + i * tickStep) * PI.toFloat() / 180f
                drawLine(
                    color       = Color(0xFF999999),
                    start       = Offset(cx + innerRadius * cos(angleRad), cy + innerRadius * sin(angleRad)),
                    end         = Offset(cx + radius * cos(angleRad), cy + radius * sin(angleRad)),
                    strokeWidth = 2.dp.toPx(),
                )
            }

            // Igła
            val needleRad = needleAngle * PI.toFloat() / 180f
            val needleLen = radius - 22.dp.toPx()
            drawLine(
                color       = CarRed,
                start       = Offset(cx, cy),
                end         = Offset(cx + needleLen * cos(needleRad), cy + needleLen * sin(needleRad)),
                strokeWidth = 4.dp.toPx(),
                cap         = StrokeCap.Round,
            )

            // Centrum
            drawCircle(Color(0xFFDDDDDD), radius = 10.dp.toPx(), center = Offset(cx, cy))
            drawCircle(CarRed,            radius = 5.dp.toPx(),  center = Offset(cx, cy))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text          = "%03d".format(speed),
                color         = CarRed,
                fontSize      = 38.sp,
                fontWeight    = FontWeight.Black,
                letterSpacing = 1.sp,
            )
            Text(
                text          = "KM/H",
                color         = CarRed.copy(alpha = 0.6f),
                fontSize      = 11.sp,
                letterSpacing = 4.sp,
            )
        }
    }
}

private const val NEEDLE_MIN = 135f
private const val NEEDLE_MAX = 405f
private const val MAX_KMH    = 260

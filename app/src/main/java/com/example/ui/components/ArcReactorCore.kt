package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeechStatus
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanBright
import com.example.ui.theme.ArcGold
import com.example.ui.theme.ArcGoldBright
import com.example.ui.theme.HudGreenOnline
import com.example.ui.theme.HudRedAlert
import com.example.ui.theme.OrbitronFontFamily
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorCore(
    speechStatus: SpeechStatus,
    audioAmplitude: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hud_rotation")

    // Rotation animations
    val rotationFast by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (speechStatus) {
                    SpeechStatus.PROCESSING -> 3000
                    SpeechStatus.LISTENING -> 5000
                    SpeechStatus.SPEAKING -> 6000
                    else -> 14000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_fast"
    )

    val rotationReverse by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (speechStatus) {
                    SpeechStatus.PROCESSING -> 4000
                    SpeechStatus.LISTENING -> 7000
                    SpeechStatus.SPEAKING -> 8000
                    else -> 18000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_reverse"
    )

    // Breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    val primaryColor = when (speechStatus) {
        SpeechStatus.LISTENING -> ArcCyanBright
        SpeechStatus.PROCESSING -> ArcGoldBright
        SpeechStatus.SPEAKING -> HudGreenOnline
        SpeechStatus.ERROR -> HudRedAlert
        SpeechStatus.STANDBY -> ArcCyan
    }

    val secondaryColor = when (speechStatus) {
        SpeechStatus.LISTENING -> ArcCyan
        SpeechStatus.PROCESSING -> ArcGold
        SpeechStatus.SPEAKING -> ArcCyan
        SpeechStatus.ERROR -> Color(0xFF880022)
        SpeechStatus.STANDBY -> Color(0xFF005662)
    }

    // Dynamic amplitude scaling
    val dynamicScale = 1f + (audioAmplitude * 0.35f)

    Box(
        modifier = modifier
            .size(240.dp)
            .testTag("arc_reactor_core")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 130.dp, color = primaryColor)
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension / 2f - 16.dp.toPx()

            // 1. Ambient Glow Gradient Background
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.28f * dynamicScale * breathingPulse),
                        primaryColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.15f
                ),
                radius = baseRadius * 1.15f,
                center = center
            )

            // 2. Outer Segmented Tech Ring
            val outerRadius = baseRadius
            drawCircle(
                color = secondaryColor.copy(alpha = 0.3f),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Rotating segmented arcs (outer)
            for (i in 0 until 4) {
                val startAngle = rotationFast + (i * 90f)
                drawArc(
                    color = primaryColor,
                    startAngle = startAngle,
                    sweepAngle = 45f,
                    useCenter = false,
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Outer Tick marks
            val numTicks = 24
            for (i in 0 until numTicks) {
                val angleRad = Math.toRadians((i * (360f / numTicks) + rotationReverse).toDouble())
                val isMajor = i % 6 == 0
                val tickLength = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                val tickStartRadius = outerRadius - 4.dp.toPx()
                val tickEndRadius = tickStartRadius - tickLength

                val start = Offset(
                    (center.x + tickStartRadius * cos(angleRad)).toFloat(),
                    (center.y + tickStartRadius * sin(angleRad)).toFloat()
                )
                val end = Offset(
                    (center.x + tickEndRadius * cos(angleRad)).toFloat(),
                    (center.y + tickEndRadius * sin(angleRad)).toFloat()
                )
                drawLine(
                    color = if (isMajor) primaryColor else secondaryColor.copy(alpha = 0.5f),
                    start = start,
                    end = end,
                    strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                )
            }

            // 3. Middle Tactical Ring (Reverse Rotation)
            val midRadius = baseRadius * 0.72f
            drawCircle(
                color = secondaryColor.copy(alpha = 0.25f),
                radius = midRadius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            for (i in 0 until 3) {
                val startAngle = rotationReverse + (i * 120f)
                drawArc(
                    color = primaryColor.copy(alpha = 0.85f),
                    startAngle = startAngle,
                    sweepAngle = 60f,
                    useCenter = false,
                    topLeft = Offset(center.x - midRadius, center.y - midRadius),
                    size = Size(midRadius * 2, midRadius * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 4. Inner Waveform Reactor Orb
            val innerRadius = (baseRadius * 0.44f) * (0.92f + (audioAmplitude * 0.25f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.9f),
                        secondaryColor.copy(alpha = 0.6f),
                        primaryColor.copy(alpha = 0.15f)
                    ),
                    center = center,
                    radius = innerRadius
                ),
                radius = innerRadius,
                center = center
            )

            // Inner glowing core border
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = innerRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Pulsing Audio Frequency Bars around the core when active
            val barCount = 18
            val barBaseRadius = innerRadius + 4.dp.toPx()
            for (i in 0 until barCount) {
                val angleRad = Math.toRadians((i * (360f / barCount)).toDouble())
                val dynamicHeight = (8.dp.toPx() + (audioAmplitude * 20.dp.toPx() * (if (i % 2 == 0) 1.2f else 0.8f)))
                val barEndRadius = barBaseRadius + dynamicHeight

                val start = Offset(
                    (center.x + barBaseRadius * cos(angleRad)).toFloat(),
                    (center.y + barBaseRadius * sin(angleRad)).toFloat()
                )
                val end = Offset(
                    (center.x + barEndRadius * cos(angleRad)).toFloat(),
                    (center.y + barEndRadius * sin(angleRad)).toFloat()
                )

                drawLine(
                    color = primaryColor.copy(alpha = 0.7f + (audioAmplitude * 0.3f)),
                    start = start,
                    end = end,
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Center HUD Emblem Text
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "RUDRA",
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 2.sp,
                color = Color.White
            )
        }
    }
}

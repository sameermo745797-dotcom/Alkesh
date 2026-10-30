package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.model.SpeechStatus
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanBright
import com.example.ui.theme.ArcGold
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudCardBorder
import com.example.ui.theme.HudGreenOnline
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    speechStatus: SpeechStatus,
    audioAmplitude: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_motion")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    val activeColor = when (speechStatus) {
        SpeechStatus.LISTENING -> ArcCyanBright
        SpeechStatus.SPEAKING -> HudGreenOnline
        SpeechStatus.PROCESSING -> ArcGold
        else -> ArcCyan.copy(alpha = 0.5f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(HudBackground.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .border(1.dp, HudCardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val barCount = 36
            val barSpacing = size.width / barCount
            val barWidth = barSpacing * 0.6f
            val centerY = size.height / 2f
            val maxHeight = size.height * 0.85f

            for (i in 0 until barCount) {
                // Sine wave variation modulated by audio amplitude
                val normalizedIndex = i.toFloat() / barCount
                val sineFactor = (sin((normalizedIndex * 4 * Math.PI) + waveOffset).toFloat() + 1f) / 2f

                val baseHeight = if (speechStatus == SpeechStatus.STANDBY) 4.dp.toPx() else 8.dp.toPx()
                val dynamicHeight = (baseHeight + (sineFactor * audioAmplitude * maxHeight)).coerceIn(
                    4.dp.toPx(),
                    maxHeight
                )

                val x = i * barSpacing + (barSpacing - barWidth) / 2f
                val top = centerY - (dynamicHeight / 2f)

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            activeColor,
                            activeColor.copy(alpha = 0.4f)
                        )
                    ),
                    topLeft = Offset(x, top),
                    size = Size(barWidth, dynamicHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
            }

            // Center laser line
            drawLine(
                color = activeColor.copy(alpha = 0.6f),
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

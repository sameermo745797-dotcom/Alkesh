package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeechStatus
import com.example.model.SystemTelemetry
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanBright
import com.example.ui.theme.ArcGold
import com.example.ui.theme.ArcGoldBright
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudCardBorder
import com.example.ui.theme.HudGreenOnline
import com.example.ui.theme.HudRedAlert
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TacticalHeader(
    telemetry: SystemTelemetry,
    speechStatus: SpeechStatus,
    handsFreeMode: Boolean,
    clapEnabled: Boolean,
    lastClapDetected: Boolean,
    isMuted: Boolean,
    onToggleHandsFree: () -> Unit,
    onToggleClap: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "beacon")
    val beaconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beacon_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(HudBackground)
            .border(1.dp, HudCardBorder, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Row 1: System Title & Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .alpha(if (speechStatus != SpeechStatus.STANDBY || lastClapDetected) beaconAlpha else 1f)
                            .background(
                                when {
                                    lastClapDetected -> ArcGoldBright
                                    speechStatus == SpeechStatus.LISTENING -> ArcCyanBright
                                    speechStatus == SpeechStatus.PROCESSING -> ArcGold
                                    speechStatus == SpeechStatus.SPEAKING -> HudGreenOnline
                                    speechStatus == SpeechStatus.ERROR -> HudRedAlert
                                    else -> HudGreenOnline
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RUDRA",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = 1.5.sp,
                        color = ArcCyanBright
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "JARVIS CORE",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 10.sp,
                        color = ArcGold
                    )
                }

                // Controls: Clap Detection, Hands-Free & Mute
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clap Activation Toggle (Taali Bajao)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    lastClapDetected -> ArcGold.copy(alpha = 0.4f)
                                    clapEnabled -> ArcCyan.copy(alpha = 0.2f)
                                    else -> Color.Transparent
                                }
                            )
                            .border(
                                1.dp,
                                when {
                                    lastClapDetected -> ArcGoldBright
                                    clapEnabled -> ArcCyan
                                    else -> HudCardBorder
                                },
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onToggleClap() }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                            .testTag("clap_activation_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WavingHand,
                                contentDescription = "Clap to Activate",
                                tint = if (clapEnabled) ArcCyanBright else TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (lastClapDetected) "CLAP!" else if (clapEnabled) "CLAP ON" else "CLAP OFF",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (lastClapDetected) ArcGoldBright else if (clapEnabled) ArcCyanBright else TextSecondary
                            )
                        }
                    }

                    // Hands-Free Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (handsFreeMode) ArcCyan.copy(alpha = 0.2f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (handsFreeMode) ArcCyan else HudCardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onToggleHandsFree() }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                            .testTag("hands_free_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Hearing,
                                contentDescription = "Hands-free Mode",
                                tint = if (handsFreeMode) ArcCyan else TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (handsFreeMode) "AUTO" else "MANUAL",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (handsFreeMode) ArcCyan else TextSecondary
                            )
                        }
                    }

                    // Mute / Unmute Button
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .border(1.dp, HudCardBorder, CircleShape)
                            .clickable { onToggleMute() }
                            .testTag("mute_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Unmute Voice" else "Mute Voice",
                            tint = if (isMuted) HudRedAlert else ArcCyan,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Row 2: Status Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STATUS: ${speechStatus.name}",
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = when (speechStatus) {
                        SpeechStatus.LISTENING -> ArcCyanBright
                        SpeechStatus.PROCESSING -> ArcGold
                        SpeechStatus.SPEAKING -> HudGreenOnline
                        SpeechStatus.ERROR -> HudRedAlert
                        SpeechStatus.STANDBY -> TextSecondary
                    }
                )

                Text(
                    text = "CLAP TRIGGER: ${if (clapEnabled) "ACTIVE" else "STANDBY"}",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    color = if (clapEnabled) HudGreenOnline else TextMuted
                )

                Text(
                    text = "PWR: ${telemetry.batteryPercent}%",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    color = TextPrimary
                )
            }
        }
    }
}

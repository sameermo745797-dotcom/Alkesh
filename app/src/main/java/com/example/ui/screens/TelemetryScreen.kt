package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanBright
import com.example.ui.theme.ArcGold
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudCardBorder
import com.example.ui.theme.HudGreenOnline
import com.example.ui.theme.HudSurface
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.RudraViewModel

@Composable
fun TelemetryScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SYSTEM TELEMETRY",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArcCyan
                )
                Text(
                    text = "DIAGNOSTIC MATRIX ACTIVE",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    color = HudGreenOnline
                )
            }

            Button(
                onClick = { viewModel.processUserQuery("Run full system diagnostics and report status") },
                colors = ButtonDefaults.buttonColors(containerColor = ArcCyan.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .border(1.dp, ArcCyan, RoundedCornerShape(8.dp))
                    .testTag("run_diagnostics_button")
            ) {
                Text(
                    text = "RUN SCAN",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArcCyanBright
                )
            }
        }

        // 1. Power Cell Telemetry Card
        TelemetryCard(
            title = "POWER GRID & BATTERY",
            icon = Icons.Default.BatteryChargingFull,
            statusBadge = if (telemetry.isCharging) "CHARGING" else "DISCHARGING",
            badgeColor = if (telemetry.isCharging) HudGreenOnline else ArcGold
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "CAPACITY LEVEL",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "${telemetry.batteryPercent}%",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ArcCyanBright
                    )
                }

                LinearProgressIndicator(
                    progress = { (telemetry.batteryPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (telemetry.batteryPercent > 20) ArcCyan else Color(0xFFFF2A6D),
                    trackColor = Color(0xFF102035)
                )

                Text(
                    text = if (telemetry.isCharging) "AC Adapter linked. Power bus operating at nominal draw."
                    else "Internal power reserves engaged. Autonomous operational window optimal.",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        // 2. Memory & Compute Telemetry
        TelemetryCard(
            title = "NEURAL COMPUTATION & RAM",
            icon = Icons.Default.Memory,
            statusBadge = "NOMINAL",
            badgeColor = HudGreenOnline
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "AVAILABLE / TOTAL",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "${telemetry.availableMemoryMb} MB / ${telemetry.totalMemoryMb} MB",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 13.sp,
                        color = ArcGold
                    )
                }

                val usedRatio = if (telemetry.totalMemoryMb > 0) {
                    ((telemetry.totalMemoryMb - telemetry.availableMemoryMb).toFloat() / telemetry.totalMemoryMb).coerceIn(0f, 1f)
                } else 0.5f

                LinearProgressIndicator(
                    progress = { usedRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ArcGold,
                    trackColor = Color(0xFF102035)
                )

                Text(
                    text = "Heap allocation stable. Coroutine dispatchers running on background thread pool.",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        // 3. Orbital Comms Matrix
        TelemetryCard(
            title = "COMMUNICATIONS & NETWORK",
            icon = Icons.Default.NetworkCheck,
            statusBadge = if (telemetry.isNetworkConnected) "ONLINE" else "OFFLINE",
            badgeColor = if (telemetry.isNetworkConnected) HudGreenOnline else Color(0xFFFF2A6D)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "CONNECTION TYPE",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = telemetry.networkType,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "AI MODEL ENGINE",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "GEMINI 2.5 FLASH",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 12.sp,
                        color = ArcCyan
                    )
                }
            }
        }

        // 4. Audio & Voice Synthesizer Telemetry
        TelemetryCard(
            title = "AUDIO ENGINE & MALE SYNTHESIZER",
            icon = Icons.Default.Speed,
            statusBadge = if (isSpeaking) "SPEAKING" else "IDLE",
            badgeColor = if (isSpeaking) HudGreenOnline else TextSecondary
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "VOICE ENGINE: ANDROID MALE TTS",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 12.sp,
                    color = TextPrimary
                )
                Text(
                    text = "INPUT SENSOR: CONTINUOUS SPEECH RECOGNIZER",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 12.sp,
                    color = TextPrimary
                )
                Text(
                    text = "ACOUSTIC LATENCY: < 15MS HARDWARE BUFFER",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun TelemetryCard(
    title: String,
    icon: ImageVector,
    statusBadge: String,
    badgeColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HudSurface)
            .border(1.dp, HudCardBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ArcCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = title,
                        fontFamily = OrbitronFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.2f))
                        .border(1.dp, badgeColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = statusBadge,
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            content()
        }
    }
}

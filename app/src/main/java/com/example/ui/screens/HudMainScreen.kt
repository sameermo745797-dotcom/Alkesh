package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.SpeechStatus
import com.example.ui.components.ArcReactorCore
import com.example.ui.components.QuickDirectivesRow
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanBright
import com.example.ui.theme.ArcGold
import com.example.ui.theme.ArcGoldBright
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudCardBorder
import com.example.ui.theme.HudGreenOnline
import com.example.ui.theme.HudRedAlert
import com.example.ui.theme.HudSurface
import com.example.ui.theme.HudSurfaceElevated
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.RudraViewModel

@Composable
fun HudMainScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val speechStatus by viewModel.speechStatus.collectAsState()
    val audioAmplitude by viewModel.audioAmplitude.collectAsState()
    val liveTranscript by viewModel.liveTranscript.collectAsState()
    val lastRudraResponse by viewModel.lastRudraResponse.collectAsState()
    val handsFreeMode by viewModel.handsFreeMode.collectAsState()
    val clapEnabled by viewModel.clapActivationEnabled.collectAsState()
    val lastClapDetected by viewModel.lastClapDetected.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) {
            viewModel.startListening()
        }
    }

    fun handleMicTap() {
        if (!hasMicPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            viewModel.toggleSpeechListening()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status Alert banner if any error or clap trigger
        AnimatedVisibility(
            visible = statusMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            statusMessage?.let { msg ->
                val isClapBanner = msg.contains("CLAP")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isClapBanner) ArcGold.copy(alpha = 0.2f) else HudRedAlert.copy(alpha = 0.15f))
                        .border(1.dp, if (isClapBanner) ArcGoldBright else HudRedAlert, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = msg,
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isClapBanner) ArcGoldBright else HudRedAlert
                    )
                }
            }
        }

        // Clap-to-Activate (Taali Bajao) HUD Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (lastClapDetected) ArcGold.copy(alpha = 0.25f) else HudSurface)
                .border(
                    1.dp,
                    if (lastClapDetected) ArcGoldBright else if (clapEnabled) ArcCyan.copy(alpha = 0.4f) else HudCardBorder,
                    RoundedCornerShape(10.dp)
                )
                .clickable { viewModel.toggleClapActivation() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
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
                        imageVector = Icons.Default.WavingHand,
                        contentDescription = "Clap Sensor",
                        tint = if (lastClapDetected) ArcGoldBright else if (clapEnabled) ArcCyanBright else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = if (lastClapDetected) "ACOUSTIC CLAP DETECTED! ACTIVATING..." else "CLAP ACTIVATION (TAALI BAJAO TRIGGER)",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (lastClapDetected) ArcGoldBright else if (clapEnabled) ArcCyanBright else TextSecondary
                        )
                        Text(
                            text = if (clapEnabled) "Tap to pause • Just clap your hands to wake Rudra instantly!" else "Paused • Tap to enable clap-to-activate",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (clapEnabled) HudGreenOnline.copy(alpha = 0.2f) else Color(0x334C6685))
                        .border(1.dp, if (clapEnabled) HudGreenOnline else TextMuted, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (clapEnabled) "ARMED" else "OFF",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (clapEnabled) HudGreenOnline else TextMuted
                    )
                }
            }
        }

        // Status Indicator
        Text(
            text = when (speechStatus) {
                SpeechStatus.LISTENING -> "TRANSMITTING VOICE SENSORS - LISTENING..."
                SpeechStatus.PROCESSING -> "QUANTUM NEURAL SYNAPSE - PROCESSING..."
                SpeechStatus.SPEAKING -> "MALE VOICE HARMONICS - SPEAKING..."
                SpeechStatus.ERROR -> "SYSTEM NOTICE - READY TO RETRY"
                SpeechStatus.STANDBY -> if (clapEnabled) "STANDBY • CLAP OR TAP CORE TO TALK" else if (handsFreeMode) "HANDS-FREE ACTIVE - AWAITING VOICE" else "STANDBY - TAP CORE TO SPEAK"
            },
            fontFamily = OrbitronFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
            color = when (speechStatus) {
                SpeechStatus.LISTENING -> ArcCyanBright
                SpeechStatus.PROCESSING -> ArcGold
                SpeechStatus.SPEAKING -> HudGreenOnline
                SpeechStatus.ERROR -> HudRedAlert
                SpeechStatus.STANDBY -> if (clapEnabled) ArcGold else TextSecondary
            }
        )

        // Arc Reactor Holographic Centerpiece
        ArcReactorCore(
            speechStatus = speechStatus,
            audioAmplitude = audioAmplitude,
            onClick = { handleMicTap() }
        )

        // Audio Waveform Spectrum
        WaveformVisualizer(
            speechStatus = speechStatus,
            audioAmplitude = audioAmplitude
        )

        // Live Transcript / Spoken Output Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(HudSurface)
                .border(1.dp, HudCardBorder, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TACTICAL AUDIO LOG",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArcCyan
                    )

                    if (speechStatus == SpeechStatus.SPEAKING) {
                        IconButton(
                            onClick = { viewModel.stopSpeaking() },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("stop_speaking_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Interrupt Voice",
                                tint = HudRedAlert,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else if (lastRudraResponse.isNotBlank()) {
                        IconButton(
                            onClick = { viewModel.speakMessageAgain(lastRudraResponse) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("replay_speaking_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Replay Voice",
                                tint = ArcCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (liveTranscript.isNotBlank()) {
                    Text(
                        text = "YOU: \"$liveTranscript\"",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                Text(
                    text = "RUDRA: $lastRudraResponse",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = TextPrimary
                )
            }
        }

        // Quick Directives
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "JARVIS TACTICAL DIRECTIVES",
                fontFamily = OrbitronFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            QuickDirectivesRow(
                onDirectiveClick = { prompt ->
                    viewModel.processUserQuery(prompt)
                }
            )
        }

        // Voice Action & Silent Text Input Fallback
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = {
                    Text(
                        text = "Or type directive (English / Hindi)...",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ArcCyan,
                    unfocusedBorderColor = HudCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = ArcCyan
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("text_directive_input")
            )

            // Send Text Button
            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.processUserQuery(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(HudSurfaceElevated)
                    .border(1.dp, HudCardBorder, RoundedCornerShape(12.dp))
                    .testTag("send_directive_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Directive",
                    tint = ArcCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Big Mic Action Button
            IconButton(
                onClick = { handleMicTap() },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (speechStatus) {
                            SpeechStatus.LISTENING -> ArcCyanBright
                            SpeechStatus.SPEAKING -> HudGreenOnline
                            SpeechStatus.PROCESSING -> ArcGold
                            else -> ArcCyan.copy(alpha = 0.25f)
                        }
                    )
                    .border(
                        1.5.dp,
                        if (speechStatus == SpeechStatus.LISTENING) ArcCyanBright else ArcCyan,
                        CircleShape
                    )
                    .testTag("mic_action_button")
            ) {
                Icon(
                    imageVector = when (speechStatus) {
                        SpeechStatus.LISTENING -> Icons.Default.Mic
                        SpeechStatus.SPEAKING -> Icons.AutoMirrored.Filled.VolumeUp
                        else -> Icons.Default.Mic
                    },
                    contentDescription = "Activate Voice Input",
                    tint = if (speechStatus == SpeechStatus.LISTENING || speechStatus == SpeechStatus.SPEAKING) {
                        Color(0xFF001F28)
                    } else {
                        ArcCyan
                    },
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

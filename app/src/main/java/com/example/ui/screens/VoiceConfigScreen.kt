package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.speech.RudraTextToSpeech
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanBright
import com.example.ui.theme.ArcGold
import com.example.ui.theme.ArcGoldBright
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudCardBorder
import com.example.ui.theme.HudGreenOnline
import com.example.ui.theme.HudSurface
import com.example.ui.theme.HudSurfaceElevated
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.RudraViewModel

@Composable
fun VoiceConfigScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentProfile by viewModel.currentProfile.collectAsState()
    val pitch by viewModel.pitch.collectAsState()
    val speed by viewModel.speed.collectAsState()
    val handsFreeMode by viewModel.handsFreeMode.collectAsState()
    val clapEnabled by viewModel.clapActivationEnabled.collectAsState()

    var customKeyInput by remember {
        mutableStateOf(viewModel.geminiService.getEffectiveApiKey())
    }

    val isKeyActive = viewModel.geminiService.isApiKeyConfigured()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Title
        Column {
            Text(
                text = "JARVIS VOICE & HARDWARE MATRIX",
                fontFamily = OrbitronFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ArcCyan
            )
            Text(
                text = "MALE PERSONA, CLAP-TO-ACTIVATE & GEMINI API CONFIG",
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 10.sp,
                color = TextSecondary
            )
        }

        // Section 0: Clap-to-Activate Trigger (Taali Bajao)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(HudSurface)
                .border(1.dp, HudCardBorder, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.WavingHand,
                        contentDescription = null,
                        tint = ArcGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "CLAP TO ACTIVATE (ताली बजाओ)",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Acoustic sensor continuously detects hand claps to instantly wake Rudra and listen for directives without touching your mobile device.",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Switch(
                    checked = clapEnabled,
                    onCheckedChange = { viewModel.setClapActivation(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ArcGold,
                        checkedTrackColor = ArcGold.copy(alpha = 0.4f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = Color(0xFF102035)
                    ),
                    modifier = Modifier.testTag("clap_config_switch")
                )
            }
        }

        // Section 1: Male Voice Profiles
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(HudSurface)
                .border(1.dp, HudCardBorder, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = ArcCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "MALE VOICE PERSONALITY",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RudraTextToSpeech.DEFAULT_PROFILES.forEach { profile ->
                        val isSelected = currentProfile.id == profile.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ArcCyan.copy(alpha = 0.15f) else HudSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) ArcCyan else HudCardBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setVoiceProfile(profile) }
                                .padding(12.dp)
                                .testTag("voice_profile_${profile.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = profile.displayName,
                                        fontFamily = OrbitronFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) ArcCyanBright else TextPrimary
                                    )
                                    Text(
                                        text = profile.description,
                                        fontFamily = ShareTechMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = ArcCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Pitch Slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "VOICE PITCH (BARITONE DEPTH)",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = String.format("%.2fx", pitch),
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 11.sp,
                            color = ArcGold
                        )
                    }
                    Slider(
                        value = pitch,
                        onValueChange = { viewModel.setPitch(it) },
                        valueRange = 0.65f..1.35f,
                        colors = SliderDefaults.colors(
                            thumbColor = ArcCyan,
                            activeTrackColor = ArcCyan,
                            inactiveTrackColor = Color(0xFF102035)
                        ),
                        modifier = Modifier.testTag("pitch_slider")
                    )
                }

                // Speed Slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "CADENCE SPEED",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = String.format("%.2fx", speed),
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 11.sp,
                            color = ArcGold
                        )
                    }
                    Slider(
                        value = speed,
                        onValueChange = { viewModel.setSpeed(it) },
                        valueRange = 0.75f..1.4f,
                        colors = SliderDefaults.colors(
                            thumbColor = ArcCyan,
                            activeTrackColor = ArcCyan,
                            inactiveTrackColor = Color(0xFF102035)
                        ),
                        modifier = Modifier.testTag("speed_slider")
                    )
                }

                Button(
                    onClick = { viewModel.testMaleVoice() },
                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ArcCyan, RoundedCornerShape(8.dp))
                        .testTag("test_voice_button")
                ) {
                    Text(
                        text = "TEST RUDRA MALE VOICE",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArcCyanBright
                    )
                }
            }
        }

        // Section 2: Hands-Free Continuous Speech Loop
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(HudSurface)
                .border(1.dp, HudCardBorder, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HANDS-FREE CONVERSATION LOOP",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Automatically listen for your reply after Rudra speaks for a natural back-and-forth speech dialogue.",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Switch(
                    checked = handsFreeMode,
                    onCheckedChange = { viewModel.setHandsFree(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ArcCyan,
                        checkedTrackColor = ArcCyan.copy(alpha = 0.4f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = Color(0xFF102035)
                    ),
                    modifier = Modifier.testTag("hands_free_switch")
                )
            }
        }

        // Section 3: Speech to Speech API Key Matrix
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(HudSurface)
                .border(1.dp, HudCardBorder, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = ArcCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "GEMINI SPEECH API KEY",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isKeyActive) HudGreenOnline.copy(alpha = 0.2f)
                                else ArcGold.copy(alpha = 0.2f)
                            )
                            .border(
                                1.dp,
                                if (isKeyActive) HudGreenOnline else ArcGold,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isKeyActive) "ORBITAL LINK ONLINE" else "LOCAL TACTICAL STANDBY",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isKeyActive) HudGreenOnline else ArcGold
                        )
                    }
                }

                Text(
                    text = if (isKeyActive) {
                        "Gemini API Key detected. Connected to Google Gemini 2.5 Flash for high-speed voice synthesis and complete Jarvis intelligence."
                    } else {
                        "No GEMINI_API_KEY detected in build config. Rudra is operating in Local Tactical Mode (with built-in commands and speech responses). Configure GEMINI_API_KEY in the AI Studio Secrets panel or enter below."
                    },
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = customKeyInput,
                    onValueChange = { customKeyInput = it },
                    label = {
                        Text(
                            text = "GEMINI_API_KEY (Optional Override)",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    },
                    placeholder = {
                        Text(
                            text = "AIzaSy...",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcCyan,
                        unfocusedBorderColor = HudCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            viewModel.geminiService.setCustomApiKey(customKeyInput)
                            Toast.makeText(context, "API Key updated", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ArcCyan.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(1.dp, ArcCyan, RoundedCornerShape(8.dp))
                            .testTag("apply_key_button")
                    ) {
                        Text(
                            text = "APPLY KEY",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArcCyanBright
                        )
                    }
                }
            }
        }

        // Section 4: Home & Lock Screen Wallpaper Matrix
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(HudSurface)
                .border(1.dp, HudCardBorder, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = ArcCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "RUDRA HUD WALLPAPER & LOCK SCREEN",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "Apply high-definition Rudra Arc Reactor HUD wallpaper directly to your Home Screen, Lock Screen, or download 1080p asset for your Laptop/PC.",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.applyWallpaper(com.example.util.WallpaperTarget.BOTH) },
                        colors = ButtonDefaults.buttonColors(containerColor = ArcCyan.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, ArcCyan, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "SET HOME & LOCK",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArcCyanBright
                        )
                    }

                    Button(
                        onClick = { viewModel.applyWallpaper(com.example.util.WallpaperTarget.LOCK) },
                        colors = ButtonDefaults.buttonColors(containerColor = ArcGold.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, ArcGold, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "LOCK SCREEN",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArcGoldBright
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

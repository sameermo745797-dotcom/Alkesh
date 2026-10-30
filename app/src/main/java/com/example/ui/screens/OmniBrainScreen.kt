package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiBrainMode
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
import com.example.viewmodel.HudTab
import com.example.viewmodel.RudraViewModel

@Composable
fun OmniBrainScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val selectedBrainMode by viewModel.selectedBrainMode.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Banner
        Column {
            Text(
                text = "GLOBAL OMNI-AI ENGINE",
                fontFamily = OrbitronFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ArcCyan
            )
            Text(
                text = "JARVIS • CHATGPT • CLAUDE • DEEPSEEK • PERPLEXITY • MIDJOURNEY",
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 10.sp,
                color = ArcGold
            )
        }

        // Active Engine Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ArcCyan.copy(alpha = 0.15f))
                .border(1.dp, ArcCyanBright, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = ArcCyanBright,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "ACTIVE REASONING: ${selectedBrainMode.title}",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = ArcCyanBright
                    )
                    Text(
                        text = selectedBrainMode.description,
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                }
            }
        }

        Text(
            text = "SWITCH AI REASONING CORE",
            fontFamily = OrbitronFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )

        // Brain Modes List
        AiBrainMode.entries.forEach { mode ->
            val isSelected = selectedBrainMode == mode
            val modeIcon: ImageVector = when (mode) {
                AiBrainMode.JARVIS -> Icons.Default.SmartToy
                AiBrainMode.CHATGPT_RESEARCH -> Icons.Default.Psychology
                AiBrainMode.CLAUDE_CODING -> Icons.Default.Code
                AiBrainMode.DEEPSEEK_REASONING -> Icons.Default.AutoAwesome
                AiBrainMode.PERPLEXITY_SEARCH -> Icons.Default.Search
                AiBrainMode.CREATIVE_PROMPTER -> Icons.Default.Image
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) HudSurfaceElevated else HudSurface)
                    .border(
                        1.dp,
                        if (isSelected) ArcCyan else HudCardBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.setBrainMode(mode) }
                    .padding(14.dp)
                    .testTag("brain_mode_${mode.name.lowercase()}")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = modeIcon,
                                contentDescription = null,
                                tint = if (isSelected) ArcCyanBright else ArcGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = mode.title,
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = if (isSelected) ArcCyanBright else TextPrimary
                                )
                                Text(
                                    text = mode.subtitle,
                                    fontFamily = ShareTechMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = ArcGold
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.setBrainMode(mode) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) ArcCyan.copy(alpha = 0.3f) else ArcGold.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.border(
                                1.dp,
                                if (isSelected) ArcCyan else ArcGold,
                                RoundedCornerShape(6.dp)
                            )
                        ) {
                            Text(
                                text = if (isSelected) "ENGAGED" else "ACTIVATE",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) ArcCyanBright else ArcGoldBright
                            )
                        }
                    }

                    Text(
                        text = mode.description,
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.5.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Quick Global AI Action Chips
        Text(
            text = "ONE-TAP WORLD AI DIRECTIVES",
            fontFamily = OrbitronFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )

        val quickActions = listOf(
            "Claude Code" to "Write a clean Kotlin Jetpack Compose function with state flow",
            "DeepSeek Math" to "Calculate step-by-step orbital escape velocity from Earth",
            "Perplexity Facts" to "Summarize the latest breakthroughs in artificial general intelligence",
            "Midjourney Prompt" to "Generate a cinematic hyperrealistic sci-fi arc reactor prompt",
            "ChatGPT Creative" to "Brainstorm 5 innovative startup ideas combining AI and robotics"
        )

        quickActions.forEach { (label, prompt) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(HudSurface)
                    .border(1.dp, HudCardBorder, RoundedCornerShape(8.dp))
                    .clickable {
                        viewModel.setActiveTab(HudTab.CORE)
                        viewModel.processUserQuery(prompt)
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ArcCyan
                        )
                        Text(
                            text = prompt,
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Run",
                        tint = ArcGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

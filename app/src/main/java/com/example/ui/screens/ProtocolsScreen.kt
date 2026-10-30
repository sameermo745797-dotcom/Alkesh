package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ArmorProtocol
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
fun ProtocolsScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val activeArmorProtocol by viewModel.activeArmorProtocol.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Column {
            Text(
                text = "STARK DEFENSE PROTOCOLS",
                fontFamily = OrbitronFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ArcCyan
            )
            Text(
                text = "AUTONOMOUS JARVIS ARMOR & TACTICAL DEPLOYMENT",
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 10.sp,
                color = ArcGold
            )
        }

        // Active Protocol Alert
        activeArmorProtocol?.let { protocol ->
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = ArcCyanBright,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "CURRENT ENGAGEMENT: ${protocol.codeName}",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = ArcCyanBright
                        )
                        Text(
                            text = protocol.quote,
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Protocols List
        Text(
            text = "AUTHORIZATION CLEARANCE: MR. STARK / SIR",
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 11.sp,
            color = TextMuted
        )

        ArmorProtocol.entries.forEach { protocol ->
            val isCurrent = activeArmorProtocol == protocol

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isCurrent) HudSurfaceElevated else HudSurface)
                    .border(
                        1.dp,
                        if (isCurrent) ArcCyan else HudCardBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.deployArmorProtocol(protocol) }
                    .padding(14.dp)
                    .testTag("protocol_card_${protocol.name.lowercase()}")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                imageVector = if (isCurrent) Icons.Default.CheckCircle else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isCurrent) HudGreenOnline else ArcGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = protocol.codeName,
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isCurrent) ArcCyanBright else TextPrimary
                            )
                        }

                        Button(
                            onClick = { viewModel.deployArmorProtocol(protocol) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrent) ArcCyan.copy(alpha = 0.3f) else ArcGold.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .border(1.dp, if (isCurrent) ArcCyan else ArcGold, RoundedCornerShape(6.dp))
                                .testTag("deploy_btn_${protocol.name.lowercase()}")
                        ) {
                            Text(
                                text = if (isCurrent) "ACTIVE" else "ENGAGE",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) ArcCyanBright else ArcGoldBright
                            )
                        }
                    }

                    Text(
                        text = protocol.description,
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Text(
                        text = "VOICE CONFIRMATION: \"${protocol.quote}\"",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

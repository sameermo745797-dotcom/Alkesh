package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanBright
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudCardBorder
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.TextMuted
import com.example.viewmodel.HudTab

data class NavItem(val tab: HudTab, val label: String, val icon: ImageVector)

val NAV_ITEMS = listOf(
    NavItem(HudTab.CORE, "CORE", Icons.Default.Mic),
    NavItem(HudTab.OMNI_BRAIN, "OMNI AI", Icons.Default.AutoAwesome),
    NavItem(HudTab.PROTOCOLS, "ARMOR", Icons.Default.Security),
    NavItem(HudTab.LOG, "LOGS", Icons.Default.ChatBubbleOutline),
    NavItem(HudTab.TELEMETRY, "MATRIX", Icons.Default.BarChart),
    NavItem(HudTab.CONFIG, "SETTINGS", Icons.Default.SettingsVoice)
)

@Composable
fun HudBottomBar(
    currentTab: HudTab,
    onTabSelected: (HudTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(HudBackground)
            .border(1.dp, HudCardBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 2.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NAV_ITEMS.forEach { item ->
                val isSelected = currentTab == item.tab
                val tint = if (isSelected) ArcCyanBright else TextMuted

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTabSelected(item.tab) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("nav_tab_${item.tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = tint,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = item.label,
                        fontFamily = OrbitronFontFamily,
                        fontSize = 8.sp,
                        color = tint
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(width = 12.dp, height = 2.dp)
                                .background(ArcCyan, RoundedCornerShape(1.dp))
                        )
                    }
                }
            }
        }
    }
}

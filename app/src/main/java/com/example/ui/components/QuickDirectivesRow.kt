package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcGold
import com.example.ui.theme.HudCardBorder
import com.example.ui.theme.HudSurfaceElevated
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.TextPrimary

data class DirectiveItem(val label: String, val prompt: String)

val DEFAULT_DIRECTIVES = listOf(
    DirectiveItem("STATUS REPORT", "Rudra, give me a status report"),
    DirectiveItem("HOUSE PARTY", "Initiate House Party Protocol"),
    DirectiveItem("MARK 85 ARMOR", "Deploy Mark 85 nano armor"),
    DirectiveItem("VERONICA HULKBUSTER", "Deploy Veronica Hulkbuster armor"),
    DirectiveItem("CLEAN SLATE", "Execute Clean Slate Protocol"),
    DirectiveItem("SENTRY MODE", "Engage perimeter tactical sentry mode"),
    DirectiveItem("ARC REACTOR SCAN", "Run full arc reactor diagnostic scan"),
    DirectiveItem("KAISE HO", "Rudra kaise ho aap?"),
    DirectiveItem("STARK JOKE", "Tell me a Tony Stark style joke")
)

@Composable
fun QuickDirectivesRow(
    onDirectiveClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(DEFAULT_DIRECTIVES) { item ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(HudSurfaceElevated)
                    .border(1.dp, HudCardBorder, RoundedCornerShape(8.dp))
                    .clickable { onDirectiveClick(item.prompt) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("directive_${item.label.lowercase().replace(" ", "_")}"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = ArcGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = item.label,
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

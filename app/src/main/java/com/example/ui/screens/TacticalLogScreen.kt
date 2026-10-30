package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.MessageSender
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcGold
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudCardBorder
import com.example.ui.theme.HudSurface
import com.example.ui.theme.HudSurfaceElevated
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.RudraViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TacticalLogScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val chatHistory by viewModel.chatHistory.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Rudra Directive", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to tactical clipboard", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TACTICAL CHRONICLE",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArcCyan
                )
                Text(
                    text = "${chatHistory.size} ENTRIES RECORDED",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            IconButton(
                onClick = { viewModel.clearHistory() },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(HudSurfaceElevated)
                    .border(1.dp, HudCardBorder, RoundedCornerShape(8.dp))
                    .testTag("clear_history_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear History",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatHistory, key = { it.id }) { msg ->
                MessageCard(
                    message = msg,
                    onPlayAudio = { viewModel.speakMessageAgain(msg.text) },
                    onCopy = { copyToClipboard(msg.text) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun MessageCard(
    message: ChatMessage,
    onPlayAudio: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == MessageSender.USER
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(message.timestamp))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isUser) HudSurfaceElevated else HudSurface)
            .border(
                1.dp,
                if (isUser) ArcCyan.copy(alpha = 0.4f) else HudCardBorder,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
            .testTag("log_entry_${message.id}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isUser) "COMMANDER" else "RUDRA",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) ArcGold else ArcCyan
                    )
                    if (!isUser && message.latencyMs > 0) {
                        Text(
                            text = " (${message.latencyMs}ms)",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Text(
                    text = timeFormat,
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            // Body text
            Text(
                text = message.text,
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = TextPrimary
            )

            // Actions for Rudra messages: replay speech, copy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy text",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                if (!isUser) {
                    IconButton(
                        onClick = onPlayAudio,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Play voice audio",
                            tint = ArcCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

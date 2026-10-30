package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.HudBottomBar
import com.example.ui.components.TacticalHeader
import com.example.ui.screens.HudMainScreen
import com.example.ui.screens.OmniBrainScreen
import com.example.ui.screens.ProtocolsScreen
import com.example.ui.screens.TacticalLogScreen
import com.example.ui.screens.TelemetryScreen
import com.example.ui.screens.VoiceConfigScreen
import com.example.ui.theme.HudBackground
import com.example.ui.theme.RudraTheme
import com.example.viewmodel.HudTab
import com.example.viewmodel.RudraViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RudraTheme {
                RudraApp()
            }
        }
    }
}

@Composable
fun RudraApp(
    viewModel: RudraViewModel = viewModel()
) {
    val activeTab by viewModel.activeTab.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val speechStatus by viewModel.speechStatus.collectAsState()
    val handsFreeMode by viewModel.handsFreeMode.collectAsState()
    val clapEnabled by viewModel.clapActivationEnabled.collectAsState()
    val lastClapDetected by viewModel.lastClapDetected.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()

    BackHandler(enabled = activeTab != HudTab.CORE) {
        viewModel.setActiveTab(HudTab.CORE)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(HudBackground),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TacticalHeader(
                telemetry = telemetry,
                speechStatus = speechStatus,
                handsFreeMode = handsFreeMode,
                clapEnabled = clapEnabled,
                lastClapDetected = lastClapDetected,
                isMuted = isMuted,
                onToggleHandsFree = { viewModel.toggleHandsFree() },
                onToggleClap = { viewModel.toggleClapActivation() },
                onToggleMute = { viewModel.toggleMute() },
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        bottomBar = {
            HudBottomBar(
                currentTab = activeTab,
                onTabSelected = { tab -> viewModel.setActiveTab(tab) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(HudBackground)
        ) {
            when (activeTab) {
                HudTab.CORE -> HudMainScreen(viewModel = viewModel)
                HudTab.OMNI_BRAIN -> OmniBrainScreen(viewModel = viewModel)
                HudTab.PROTOCOLS -> ProtocolsScreen(viewModel = viewModel)
                HudTab.LOG -> TacticalLogScreen(viewModel = viewModel)
                HudTab.TELEMETRY -> TelemetryScreen(viewModel = viewModel)
                HudTab.CONFIG -> VoiceConfigScreen(viewModel = viewModel)
            }
        }
    }
}

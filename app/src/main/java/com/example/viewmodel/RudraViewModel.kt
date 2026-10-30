package com.example.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AiBrainMode
import com.example.model.ArmorProtocol
import com.example.model.ChatMessage
import com.example.model.MessageSender
import com.example.model.SpeechStatus
import com.example.model.SystemTelemetry
import com.example.model.VoiceProfile
import com.example.service.RudraGeminiService
import com.example.speech.RudraClapDetector
import com.example.speech.RudraSpeechRecognizer
import com.example.speech.RudraTextToSpeech
import com.example.util.TelemetryHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class HudTab {
    CORE,
    PROTOCOLS,
    OMNI_BRAIN,
    LOG,
    TELEMETRY,
    CONFIG
}

class RudraViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "RudraViewModel"
    }

    private val speechRecognizer = RudraSpeechRecognizer(application)
    private val textToSpeech = RudraTextToSpeech(application, viewModelScope)
    private val clapDetector = RudraClapDetector(application, viewModelScope)
    val geminiService = RudraGeminiService(application)

    // UI States
    private val _speechStatus = MutableStateFlow(SpeechStatus.STANDBY)
    val speechStatus: StateFlow<SpeechStatus> = _speechStatus.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private val _lastRudraResponse = MutableStateFlow(
        "Rudra Omni-AI Core online. All global AI capabilities unified, Sir."
    )
    val lastRudraResponse: StateFlow<String> = _lastRudraResponse.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.RUDRA,
                text = "Rudra Omni-AI online. Jarvis, ChatGPT, Claude, DeepSeek & Perplexity cores integrated. Clap hands or speak, Sir."
            )
        )
    )
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _telemetry = MutableStateFlow(TelemetryHelper.readTelemetry(application))
    val telemetry: StateFlow<SystemTelemetry> = _telemetry.asStateFlow()

    private val _handsFreeMode = MutableStateFlow(false)
    val handsFreeMode: StateFlow<Boolean> = _handsFreeMode.asStateFlow()

    private val _clapActivationEnabled = MutableStateFlow(true)
    val clapActivationEnabled: StateFlow<Boolean> = _clapActivationEnabled.asStateFlow()

    private val _lastClapDetected = MutableStateFlow(false)
    val lastClapDetected: StateFlow<Boolean> = _lastClapDetected.asStateFlow()

    private val _activeTab = MutableStateFlow(HudTab.CORE)
    val activeTab: StateFlow<HudTab> = _activeTab.asStateFlow()

    private val _selectedBrainMode = MutableStateFlow(AiBrainMode.JARVIS)
    val selectedBrainMode: StateFlow<AiBrainMode> = _selectedBrainMode.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _activeArmorProtocol = MutableStateFlow<ArmorProtocol?>(null)
    val activeArmorProtocol: StateFlow<ArmorProtocol?> = _activeArmorProtocol.asStateFlow()

    val currentProfile: StateFlow<VoiceProfile> = textToSpeech.currentProfile
    val pitch: StateFlow<Float> = textToSpeech.pitch
    val speed: StateFlow<Float> = textToSpeech.speed
    val isSpeaking: StateFlow<Boolean> = textToSpeech.isSpeaking
    val isListening: StateFlow<Boolean> = speechRecognizer.isListening
    val isClapListening: StateFlow<Boolean> = clapDetector.isClapListening

    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        speechRecognizer.setListeners(
            onResult = { recognizedText ->
                processUserQuery(recognizedText)
            },
            onError = { errorMessage ->
                if (_speechStatus.value == SpeechStatus.LISTENING) {
                    _speechStatus.value = SpeechStatus.STANDBY
                }
                _statusMessage.value = errorMessage
                resumeClapListeningIfAllowed()
                viewModelScope.launch {
                    delay(3000)
                    if (_statusMessage.value == errorMessage) {
                        _statusMessage.value = null
                    }
                }
            }
        )

        clapDetector.setOnClapListener {
            onClapAcousticImpulse()
        }

        if (_clapActivationEnabled.value) {
            clapDetector.startListening()
        }

        viewModelScope.launch {
            speechRecognizer.audioRms.collect { rms ->
                if (_speechStatus.value == SpeechStatus.LISTENING) {
                    _audioAmplitude.value = rms
                }
            }
        }

        viewModelScope.launch {
            textToSpeech.speechAmplitude.collect { amp ->
                if (_speechStatus.value == SpeechStatus.SPEAKING) {
                    _audioAmplitude.value = amp
                }
            }
        }

        viewModelScope.launch {
            speechRecognizer.liveTranscript.collect { text ->
                if (_speechStatus.value == SpeechStatus.LISTENING) {
                    _liveTranscript.value = text
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                _telemetry.value = TelemetryHelper.readTelemetry(getApplication())
                delay(10_000)
            }
        }
    }

    private fun onClapAcousticImpulse() {
        if (!_clapActivationEnabled.value) return

        Log.d(TAG, "Clap recognized! Activating Rudra...")
        _lastClapDetected.value = true
        _statusMessage.value = "CLAP IMPULSE DETECTED! ACTIVATING RUDRA..."

        viewModelScope.launch {
            delay(1500)
            _lastClapDetected.value = false
            if (_statusMessage.value?.startsWith("CLAP IMPULSE") == true) {
                _statusMessage.value = null
            }
        }

        pauseClapListening()
        val clapAcknowledge = "At your service, Sir. Listening."
        _lastRudraResponse.value = clapAcknowledge
        if (!_isMuted.value) {
            _speechStatus.value = SpeechStatus.SPEAKING
            textToSpeech.speak(clapAcknowledge) {
                mainHandler.post {
                    startListening()
                }
            }
        } else {
            startListening()
        }
    }

    fun setBrainMode(mode: AiBrainMode) {
        _selectedBrainMode.value = mode
        val announce = "Omni AI switched to ${mode.title}, Sir."
        _statusMessage.value = announce
        viewModelScope.launch {
            delay(2500)
            if (_statusMessage.value == announce) {
                _statusMessage.value = null
            }
        }
    }

    fun toggleClapActivation() {
        val next = !_clapActivationEnabled.value
        _clapActivationEnabled.value = next
        if (next) {
            clapDetector.startListening()
            _statusMessage.value = "CLAP ACTIVATION: ENGAGED"
        } else {
            clapDetector.stopListening()
            _statusMessage.value = "CLAP ACTIVATION: DISENGAGED"
        }
        viewModelScope.launch {
            delay(2000)
            _statusMessage.value = null
        }
    }

    fun setClapActivation(enabled: Boolean) {
        _clapActivationEnabled.value = enabled
        if (enabled) {
            clapDetector.startListening()
        } else {
            clapDetector.stopListening()
        }
    }

    private fun pauseClapListening() {
        clapDetector.pauseForSpeech()
    }

    private fun resumeClapListeningIfAllowed() {
        if (_clapActivationEnabled.value && _speechStatus.value == SpeechStatus.STANDBY) {
            clapDetector.startListening()
        }
    }

    fun setActiveTab(tab: HudTab) {
        _activeTab.value = tab
    }

    fun toggleSpeechListening() {
        if (_speechStatus.value == SpeechStatus.SPEAKING) {
            stopSpeaking()
            return
        }

        if (_speechStatus.value == SpeechStatus.LISTENING) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun startListening() {
        stopSpeaking()
        pauseClapListening()
        _liveTranscript.value = ""
        _speechStatus.value = SpeechStatus.LISTENING
        mainHandler.post {
            speechRecognizer.startListening()
        }
    }

    fun stopListening() {
        mainHandler.post {
            speechRecognizer.stopListening()
        }
        _speechStatus.value = SpeechStatus.STANDBY
        _audioAmplitude.value = 0f
        resumeClapListeningIfAllowed()
    }

    fun stopSpeaking() {
        textToSpeech.stop()
        _speechStatus.value = SpeechStatus.STANDBY
        _audioAmplitude.value = 0f
        resumeClapListeningIfAllowed()
    }

    fun toggleHandsFree() {
        val next = !_handsFreeMode.value
        _handsFreeMode.value = next
        if (next && _speechStatus.value == SpeechStatus.STANDBY) {
            startListening()
        }
    }

    fun setHandsFree(enabled: Boolean) {
        _handsFreeMode.value = enabled
    }

    fun toggleMute() {
        val next = !_isMuted.value
        _isMuted.value = next
        if (next) {
            textToSpeech.stop()
        }
    }

    fun deployArmorProtocol(protocol: ArmorProtocol) {
        _activeArmorProtocol.value = protocol
        val prompt = "Deploy ${protocol.codeName}"
        val userMsg = ChatMessage(sender = MessageSender.USER, text = prompt)
        _chatHistory.value = _chatHistory.value + userMsg

        _lastRudraResponse.value = protocol.quote
        val rudraMsg = ChatMessage(
            sender = MessageSender.RUDRA,
            text = protocol.quote
        )
        _chatHistory.value = _chatHistory.value + rudraMsg

        if (!_isMuted.value) {
            pauseClapListening()
            _speechStatus.value = SpeechStatus.SPEAKING
            textToSpeech.speak(protocol.quote) {
                mainHandler.post {
                    onRudraFinishedSpeaking()
                }
            }
        }
    }

    fun processUserQuery(prompt: String) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isEmpty()) return

        stopListening()
        pauseClapListening()
        _speechStatus.value = SpeechStatus.PROCESSING
        _liveTranscript.value = cleanPrompt

        val userMsg = ChatMessage(sender = MessageSender.USER, text = cleanPrompt)
        _chatHistory.value = _chatHistory.value + userMsg

        viewModelScope.launch {
            try {
                val (response, latencyMs) = geminiService.processQuery(
                    userPrompt = cleanPrompt,
                    history = _chatHistory.value,
                    telemetry = _telemetry.value,
                    brainMode = _selectedBrainMode.value
                )

                _lastRudraResponse.value = response
                val rudraMsg = ChatMessage(
                    sender = MessageSender.RUDRA,
                    text = response,
                    latencyMs = latencyMs
                )
                _chatHistory.value = _chatHistory.value + rudraMsg

                if (!_isMuted.value) {
                    _speechStatus.value = SpeechStatus.SPEAKING
                    textToSpeech.speak(response) {
                        mainHandler.post {
                            onRudraFinishedSpeaking()
                        }
                    }
                } else {
                    _speechStatus.value = SpeechStatus.STANDBY
                    if (_handsFreeMode.value) {
                        delay(600)
                        startListening()
                    } else {
                        resumeClapListeningIfAllowed()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process query", e)
                val fallbackText = "Neural query interrupted, Sir. Re-engaging standby."
                _lastRudraResponse.value = fallbackText
                _speechStatus.value = SpeechStatus.STANDBY
                resumeClapListeningIfAllowed()
            }
        }
    }

    private fun onRudraFinishedSpeaking() {
        _speechStatus.value = SpeechStatus.STANDBY
        _audioAmplitude.value = 0f

        if (_handsFreeMode.value) {
            viewModelScope.launch {
                delay(500)
                if (_handsFreeMode.value && _speechStatus.value == SpeechStatus.STANDBY) {
                    startListening()
                }
            }
        } else {
            resumeClapListeningIfAllowed()
        }
    }

    fun speakMessageAgain(text: String) {
        if (_speechStatus.value == SpeechStatus.SPEAKING) {
            stopSpeaking()
            return
        }
        stopListening()
        pauseClapListening()
        _speechStatus.value = SpeechStatus.SPEAKING
        textToSpeech.speak(text) {
            _speechStatus.value = SpeechStatus.STANDBY
            _audioAmplitude.value = 0f
            resumeClapListeningIfAllowed()
        }
    }

    fun setVoiceProfile(profile: VoiceProfile) {
        textToSpeech.setProfile(profile)
    }

    fun setPitch(pitch: Float) {
        textToSpeech.setPitch(pitch)
    }

    fun setSpeed(speed: Float) {
        textToSpeech.setSpeed(speed)
    }

    fun testMaleVoice() {
        val testPhrase = "Rudra Omni-AI synthesizer nominal, Sir. All world AI engines are online and synchronized."
        textToSpeech.speak(testPhrase)
    }

    fun applyWallpaper(target: com.example.util.WallpaperTarget) {
        viewModelScope.launch {
            val success = com.example.util.WallpaperHelper.setDeviceWallpaper(getApplication(), target)
            val msg = if (success) {
                when (target) {
                    com.example.util.WallpaperTarget.BOTH -> "RUDRA ARC REACTOR APPLIED TO HOME & LOCK SCREEN!"
                    com.example.util.WallpaperTarget.HOME -> "RUDRA ARC REACTOR APPLIED TO HOME SCREEN!"
                    com.example.util.WallpaperTarget.LOCK -> "RUDRA ARC REACTOR APPLIED TO LOCK SCREEN!"
                }
            } else {
                "COULD NOT APPLY SYSTEM WALLPAPER"
            }
            _statusMessage.value = msg

            val spokenAcknowledge = if (success) {
                "Wallpaper applied successfully, Sir. Your device is now branded with the Rudra Arc Reactor matrix."
            } else {
                "Unable to apply wallpaper automatically, Sir. Please check system permissions."
            }
            if (!_isMuted.value) {
                textToSpeech.speak(spokenAcknowledge)
            }

            delay(3500)
            if (_statusMessage.value == msg) {
                _statusMessage.value = null
            }
        }
    }

    fun clearHistory() {
        _chatHistory.value = listOf(
            ChatMessage(
                sender = MessageSender.RUDRA,
                text = "Tactical logs cleared. Rudra Core reset to operational baseline, Sir."
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        clapDetector.stopListening()
        speechRecognizer.destroy()
        textToSpeech.shutdown()
    }
}

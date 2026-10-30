package com.example.speech

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import com.example.model.VoiceProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

class RudraTextToSpeech(
    private val context: Context,
    private val scope: CoroutineScope
) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "RudraTTS"
        val DEFAULT_PROFILES = listOf(
            VoiceProfile(
                id = "jarvis_baritone",
                displayName = "Rudra Prime (Jarvis Male)",
                pitch = 0.85f,
                speed = 1.02f,
                description = "Deep baritone, authoritative & articulate tactical persona"
            ),
            VoiceProfile(
                id = "tactical_commander",
                displayName = "Commander (Crisp Male)",
                pitch = 0.92f,
                speed = 1.10f,
                description = "Brisk, high-efficiency military tactical tone"
            ),
            VoiceProfile(
                id = "cyber_deep",
                displayName = "Quantum Synthetic (Deep)",
                pitch = 0.78f,
                speed = 0.95f,
                description = "Resonant holographic synthesizer tone"
            )
        )
    }

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speechAmplitude = MutableStateFlow(0f)
    val speechAmplitude: StateFlow<Float> = _speechAmplitude.asStateFlow()

    private val _currentProfile = MutableStateFlow(DEFAULT_PROFILES[0])
    val currentProfile: StateFlow<VoiceProfile> = _currentProfile.asStateFlow()

    private val _pitch = MutableStateFlow(0.85f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _speed = MutableStateFlow(1.02f)
    val speed: StateFlow<Float> = _speed.asStateFlow()

    private val _availableMaleVoices = MutableStateFlow<List<String>>(emptyList())
    val availableMaleVoices: StateFlow<List<String>> = _availableMaleVoices.asStateFlow()

    private var onSpeechDoneCallback: (() -> Unit)? = null
    private var amplitudeJob: Job? = null

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ttsEngine = tts ?: return
            val result = ttsEngine.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                ttsEngine.setLanguage(Locale.getDefault())
            }

            configureMaleVoice(ttsEngine)
            applyPitchAndSpeed()

            ttsEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    startVisualizerPulsing()
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    stopVisualizerPulsing()
                    onSpeechDoneCallback?.invoke()
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    stopVisualizerPulsing()
                    Log.e(TAG, "TTS Utterance error: $utteranceId")
                }
            })

            isInitialized = true
            Log.d(TAG, "Rudra Male TTS initialized successfully")
        } else {
            Log.e(TAG, "Rudra TTS initialization failed: $status")
        }
    }

    private fun configureMaleVoice(ttsEngine: TextToSpeech) {
        try {
            val voices: Set<Voice>? = ttsEngine.voices
            if (!voices.isNullOrEmpty()) {
                val maleVoices = voices.filter { voice ->
                    val name = voice.name.lowercase(Locale.ROOT)
                    name.contains("male") ||
                            name.contains("en-us-x-sfg") ||
                            name.contains("en-gb-x-rjs") ||
                            name.contains("en-us-x-tpd") ||
                            name.contains("en-in-x-cxx")
                }

                _availableMaleVoices.value = maleVoices.map { it.name }

                // Pick best male voice or first male voice
                val selectedMaleVoice = maleVoices.firstOrNull { it.locale.language == "en" }
                    ?: maleVoices.firstOrNull()

                if (selectedMaleVoice != null) {
                    ttsEngine.voice = selectedMaleVoice
                    Log.d(TAG, "Selected male voice: ${selectedMaleVoice.name}")
                } else {
                    Log.d(TAG, "No explicit male voice tag found; using pitch modulation for male tone.")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Voice discovery encountered issue, falling back to pitch modulation: ${e.message}")
        }
    }

    fun setProfile(profile: VoiceProfile) {
        _currentProfile.value = profile
        _pitch.value = profile.pitch
        _speed.value = profile.speed
        applyPitchAndSpeed()
    }

    fun setPitch(newPitch: Float) {
        _pitch.value = newPitch.coerceIn(0.5f, 2.0f)
        applyPitchAndSpeed()
    }

    fun setSpeed(newSpeed: Float) {
        _speed.value = newSpeed.coerceIn(0.5f, 2.0f)
        applyPitchAndSpeed()
    }

    private fun applyPitchAndSpeed() {
        tts?.setPitch(_pitch.value)
        tts?.setSpeechRate(_speed.value)
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        this.onSpeechDoneCallback = onComplete

        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not ready yet")
            onComplete?.invoke()
            return
        }

        stop()
        applyPitchAndSpeed()

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }

        val utteranceId = "rudra_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        stopVisualizerPulsing()
    }

    private fun startVisualizerPulsing() {
        amplitudeJob?.cancel()
        amplitudeJob = scope.launch(Dispatchers.Default) {
            while (isActive && _isSpeaking.value) {
                // Generate a realistic vocal amplitude pulse
                val base = 0.45f + (Random.nextFloat() * 0.55f)
                _speechAmplitude.value = base
                delay(70)
            }
            _speechAmplitude.value = 0f
        }
    }

    private fun stopVisualizerPulsing() {
        amplitudeJob?.cancel()
        _speechAmplitude.value = 0f
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}

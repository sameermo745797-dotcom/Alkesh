package com.example.speech

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * RudraClapDetector
 * Listens in background/standby for sharp acoustic transient peaks (claps).
 * Automatically fires onClapDetected() to activate Rudra without touching the phone!
 */
class RudraClapDetector(
    private val context: Context,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "RudraClapDetector"
        private const val SAMPLE_RATE = 16000
        private const val CLAP_RMS_THRESHOLD = 7500.0 // Sound pressure impulse threshold for a clap
        private const val CLAP_COOLDOWN_MS = 1200L    // Debounce window to prevent double trigger
    }

    private var audioRecord: AudioRecord? = null
    private var listeningJob: Job? = null

    private val _isClapListening = MutableStateFlow(false)
    val isClapListening: StateFlow<Boolean> = _isClapListening.asStateFlow()

    private val _lastClapTimestamp = MutableStateFlow(0L)
    val lastClapTimestamp: StateFlow<Long> = _lastClapTimestamp.asStateFlow()

    private val _ambientRms = MutableStateFlow(0f)
    val ambientRms: StateFlow<Float> = _ambientRms.asStateFlow()

    private var onClapCallback: (() -> Unit)? = null
    private var lastTriggerTime = 0L

    fun setOnClapListener(listener: () -> Unit) {
        this.onClapCallback = listener
    }

    fun startListening() {
        if (_isClapListening.value) return

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            Log.w(TAG, "RECORD_AUDIO permission not granted, cannot start Clap Detector")
            return
        }

        listeningJob?.cancel()
        listeningJob = scope.launch(Dispatchers.Default) {
            try {
                val bufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(1024)

                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioRecord failed to initialize for clap detection")
                    return@launch
                }

                audioRecord?.startRecording()
                _isClapListening.value = true
                Log.d(TAG, "Rudra Clap Detector active - ready for acoustic clap trigger!")

                val buffer = ShortArray(bufferSize / 2)

                while (isActive && _isClapListening.value) {
                    val readCount = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (readCount > 0) {
                        var sumSquares = 0.0
                        var peak = 0
                        for (i in 0 until readCount) {
                            val sample = buffer[i].toInt()
                            sumSquares += sample * sample
                            val absSample = Math.abs(sample)
                            if (absSample > peak) {
                                peak = absSample
                            }
                        }

                        val rms = sqrt(sumSquares / readCount)
                        val normalizedRms = (rms / 15000.0).toFloat().coerceIn(0f, 1f)
                        _ambientRms.value = normalizedRms

                        val now = SystemClock.elapsedRealtime()
                        // A clap is a very sharp, high amplitude impulse above threshold
                        if (peak > CLAP_RMS_THRESHOLD && (now - lastTriggerTime > CLAP_COOLDOWN_MS)) {
                            lastTriggerTime = now
                            _lastClapTimestamp.value = System.currentTimeMillis()
                            Log.d(TAG, ">>> CLAP DETECTED! Peak: $peak, RMS: $rms. Triggering Rudra!")
                            scope.launch(Dispatchers.Main) {
                                onClapCallback?.invoke()
                            }
                            // Pause briefly after trigger to let user speak
                            delay(500)
                        }
                    }
                    delay(20)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Clap detection error", e)
            } finally {
                stopInternal()
            }
        }
    }

    fun pauseForSpeech() {
        // Temporarily stop clap recording while Rudra is speaking or STT is running
        stopInternal()
    }

    private fun stopInternal() {
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            // ignore
        } finally {
            audioRecord = null
            _isClapListening.value = false
            _ambientRms.value = 0f
        }
    }

    fun stopListening() {
        listeningJob?.cancel()
        stopInternal()
    }
}

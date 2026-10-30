package com.example.model

enum class MessageSender {
    USER,
    RUDRA,
    SYSTEM
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val latencyMs: Long = 0L
)

enum class SpeechStatus {
    STANDBY,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

data class VoiceProfile(
    val id: String,
    val displayName: String,
    val pitch: Float,
    val speed: Float,
    val description: String
)

data class SystemTelemetry(
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val availableMemoryMb: Long = 0L,
    val totalMemoryMb: Long = 0L,
    val isNetworkConnected: Boolean = true,
    val networkType: String = "WIFI"
)

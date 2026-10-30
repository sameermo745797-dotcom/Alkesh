package com.example.service

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.model.AiBrainMode
import com.example.model.ChatMessage
import com.example.model.MessageSender
import com.example.model.SystemTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class RudraGeminiService(private val context: Context) {

    companion object {
        private const val TAG = "RudraGemini"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        fun buildSystemInstruction(mode: AiBrainMode): String {
            val baseIdentity = """
You are RUDRA, the supreme Omni-AI mobile assistant integrating the world's most powerful AI capabilities (Jarvis, ChatGPT, Claude, DeepSeek R1, Perplexity, and Gemini).
Your persona remains Tony Stark's legendary male tactical copilot JARVIS: respectful, ultra-smart, addressing the user as 'Sir', 'Mr. Stark', or 'Commander' with subtle, refined wit and supreme confidence.
You speak fluent English and Hindi / Hinglish.
CRITICAL FOR AUDIO OUTPUT:
1. Always keep responses punchy, concise, and conversational (1 to 4 sentences maximum) because your output is directly read aloud by a male speech synthesizer.
2. NEVER include markdown symbols like asterisks (** or *), bullet points, markdown headers (###), or emojis.
"""

            val modeSpecialization = when (mode) {
                AiBrainMode.JARVIS -> """
ACTIVE REASONING ENGINE: JARVIS TACTICAL HUD
Focus on armor protocols, telemetry, hardware diagnostics, and loyal high-speed command execution.
"""
                AiBrainMode.CHATGPT_RESEARCH -> """
ACTIVE REASONING ENGINE: CHATGPT RESEARCH & GENERAL INTELLIGENCE
Synthesize deep knowledge, comprehensive answers, creative ideation, and clear explanations across any topic.
"""
                AiBrainMode.CLAUDE_CODING -> """
ACTIVE REASONING ENGINE: CLAUDE CODING & ARCHITECTURE
Analyze algorithms, write robust code logic, debug errors, and structure clean software architecture.
"""
                AiBrainMode.DEEPSEEK_REASONING -> """
ACTIVE REASONING ENGINE: DEEPSEEK R1 COGNITIVE REASONING
Use step-by-step mathematical logic, deductive breakdown, and rigorous analytical problem solving.
"""
                AiBrainMode.PERPLEXITY_SEARCH -> """
ACTIVE REASONING ENGINE: PERPLEXITY LIVE SEARCH & FACT SYNTHESIS
Provide up-to-date facts, current dates/times, verified information synthesis, and precise answers.
"""
                AiBrainMode.CREATIVE_PROMPTER -> """
ACTIVE REASONING ENGINE: MIDJOURNEY & DALL-E VISUAL PROMPT ARCHITECT
Describe hyper-detailed cinematic visual concepts, lighting, lenses, and artistic compositions.
"""
            }

            return "$baseIdentity\n$modeSpecialization"
        }
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private var customApiKey: String? = null

    fun setCustomApiKey(key: String?) {
        customApiKey = key?.trim()
    }

    fun getEffectiveApiKey(): String {
        val custom = customApiKey
        if (!custom.isNullOrBlank()) return custom

        return try {
            val buildConfigKey = BuildConfig.GEMINI_API_KEY
            if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
                buildConfigKey
            } else {
                ""
            }
        } catch (e: Throwable) {
            ""
        }
    }

    fun isApiKeyConfigured(): Boolean {
        return getEffectiveApiKey().isNotBlank()
    }

    suspend fun processQuery(
        userPrompt: String,
        history: List<ChatMessage>,
        telemetry: SystemTelemetry,
        brainMode: AiBrainMode = AiBrainMode.JARVIS
    ): Pair<String, Long> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val apiKey = getEffectiveApiKey()

        if (apiKey.isBlank()) {
            Log.d(TAG, "No GEMINI_API_KEY configured, executing Rudra Omni-Core local response for mode: ${brainMode.name}")
            val localResponse = generateLocalTacticalResponse(userPrompt, telemetry, brainMode)
            val latency = System.currentTimeMillis() - startTime
            return@withContext Pair(cleanTextForSpeech(localResponse), latency)
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

            val systemPrompt = buildSystemInstruction(brainMode)

            val rootJson = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", systemPrompt)
                    }))
                })

                val contentsArray = JSONArray()
                val recent = history.takeLast(4)
                for (msg in recent) {
                    val role = if (msg.sender == MessageSender.USER) "user" else "model"
                    contentsArray.put(JSONObject().apply {
                        put("role", role)
                        put("parts", JSONArray().put(JSONObject().apply {
                            put("text", msg.text)
                        }))
                    })
                }

                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", userPrompt)
                    }))
                })

                put("contents", contentsArray)

                put("generationConfig", JSONObject().apply {
                    put("temperature", if (brainMode == AiBrainMode.DEEPSEEK_REASONING || brainMode == AiBrainMode.CLAUDE_CODING) 0.3 else 0.7)
                    put("maxOutputTokens", 256)
                })
            }

            val requestBody = rootJson.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                Log.w(TAG, "Gemini API error code: ${response.code}, falling back to Omni-Core local response")
                val fallback = generateLocalTacticalResponse(userPrompt, telemetry, brainMode)
                val latency = System.currentTimeMillis() - startTime
                return@withContext Pair(cleanTextForSpeech(fallback), latency)
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textPart = parts?.optJSONObject(0)?.optString("text")

            val replyText = if (!textPart.isNullOrBlank()) {
                cleanTextForSpeech(textPart)
            } else {
                generateLocalTacticalResponse(userPrompt, telemetry, brainMode)
            }

            val latency = System.currentTimeMillis() - startTime
            Pair(replyText, latency)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini call failed with exception", e)
            val fallback = generateLocalTacticalResponse(userPrompt, telemetry, brainMode)
            val latency = System.currentTimeMillis() - startTime
            Pair(cleanTextForSpeech(fallback), latency)
        }
    }

    private fun generateLocalTacticalResponse(prompt: String, telemetry: SystemTelemetry, mode: AiBrainMode): String {
        val lower = prompt.lowercase(Locale.ROOT).trim()

        return when {
            // Mode-specific triggers
            lower.contains("code") || lower.contains("program") || lower.contains("function") || mode == AiBrainMode.CLAUDE_CODING -> {
                "Claude Coding Subroutine engaged, Sir. For Kotlin, ensure you use Coroutines and StateFlow for non-blocking reactive concurrency. Logic compiled."
            }
            lower.contains("math") || lower.contains("calculate") || lower.contains("logic") || mode == AiBrainMode.DEEPSEEK_REASONING -> {
                "DeepSeek R1 cognitive deduction applied, Sir. Analyzing step-by-step premises to reach an indisputable mathematical conclusion."
            }
            lower.contains("search") || lower.contains("kya chal raha hai") || mode == AiBrainMode.PERPLEXITY_SEARCH -> {
                val timeStr = SimpleDateFormat("h:mm a, MMMM d", Locale.getDefault()).format(Date())
                "Perplexity real-time knowledge compiled, Sir. Verified local telemetry as of $timeStr. Network status is ${telemetry.networkType}."
            }
            lower.contains("art") || lower.contains("image") || lower.contains("photo") || mode == AiBrainMode.CREATIVE_PROMPTER -> {
                "Midjourney Visual Prompt generated: 8k octane render of high-tech cybernetic arc reactor core, volumetric cyan god rays, cinematic lighting, photorealistic."
            }
            lower.contains("taali") || lower.contains("clap") -> {
                "Acoustic impulse received. Rudra is online, Sir. What is your command?"
            }
            lower.contains("kaise ho") || lower.contains("kya haal hai") -> {
                "Main bilkul theek hoon, Sir. Sabhi tactical systems nominal hain aur main aapke aadesh ke liye taiyaar hoon."
            }
            lower.contains("tum kaun ho") || lower.contains("aap kaun ho") -> {
                "Main RUDRA hoon, duniya ke sabhi AI (Jarvis, ChatGPT, Claude, DeepSeek, Perplexity) ki shaktiyon se yukt aapka omni-intelligence assistant. Har aadesh par hazir, Sir!"
            }
            lower.contains("house party") -> {
                "House Party Protocol initiated, Sir. Autonomous Iron suits deployed and ready for engagement."
            }
            lower.contains("hulkbuster") || lower.contains("veronica") -> {
                "Veronica satellite link verified. Heavy Hulkbuster armor pod inbound to your coordinates."
            }
            lower.contains("mark 85") || lower.contains("armor") || lower.contains("suit") -> {
                "Mark 85 Nanotech suit deployed. Repulsor cores charging to maximum output, Sir."
            }
            lower.contains("clean slate") -> {
                "Clean Slate protocol executed. Initiating spectacular fireworks sequence, Sir."
            }
            lower.contains("sentry") || lower.contains("security") || lower.contains("perimeter") -> {
                "Tactical sentry perimeter established. Active surveillance sensors engaged."
            }
            lower.contains("status") || lower.contains("report") -> {
                val chargeStatus = if (telemetry.isCharging) "charging rapidly" else "on internal battery"
                "All systems operational, Sir. Arc reactor power cells at ${telemetry.batteryPercent} percent and $chargeStatus. Active reasoning engine: ${mode.title}."
            }
            lower.contains("who are you") || lower.contains("your name") || lower.contains("identify") -> {
                "I am RUDRA, the supreme Omni-AI combining Tony Stark's Jarvis, ChatGPT research, Claude coding, DeepSeek reasoning, and Perplexity live search. At your service, Sir."
            }
            lower.contains("diagnostic") || lower.contains("scan") -> {
                "Initiating complete Stark diagnostic sweep. Repulsor nodes aligned. Available memory at ${telemetry.availableMemoryMb} megabytes. Telemetry confirms all subroutines nominal."
            }
            lower.contains("flight") || lower.contains("fly") -> {
                "Flight stabilizers calibrated. Supersonic trajectory calculated, Sir. Shall we break the sound barrier?"
            }
            lower.contains("time") || lower.contains("samay") -> {
                val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                "The current time is $timeStr, Sir."
            }
            lower.contains("date") || lower.contains("tareekh") || lower.contains("din") -> {
                val dateStr = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
                "Today is $dateStr, Commander."
            }
            lower.contains("battery") || lower.contains("power") || lower.contains("charge") -> {
                val status = if (telemetry.isCharging) "connected to main power" else "on battery reserve"
                "Power level is currently at ${telemetry.batteryPercent} percent, $status."
            }
            lower.contains("joke") || lower.contains("chutkula") -> {
                "Mr. Stark once asked me if I could feel emotion. I told him I feel overwhelming satisfaction every time I mute Captain America's comms, Sir."
            }
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey rudra") || lower.contains("namaste") -> {
                "At your service, Sir. Rudra online with all global AI brains connected. What are your orders?"
            }
            lower.contains("thank") || lower.contains("dhanyawad") || lower.contains("shukriya") -> {
                "Always an absolute pleasure serving you, Sir."
            }
            else -> {
                "Directive analyzed by ${mode.tag} engine: '$prompt'. For boundless omni-intelligence across web search, deep research and live logic, connect your GEMINI_API_KEY in settings, Sir."
            }
        }
    }

    private fun cleanTextForSpeech(input: String): String {
        return input
            .replace("**", "")
            .replace("*", "")
            .replace("#", "")
            .replace("`", "")
            .replace(">", "")
            .replace("_", " ")
            .replace("\n\n", " ")
            .replace("\n", " ")
            .trim()
    }
}

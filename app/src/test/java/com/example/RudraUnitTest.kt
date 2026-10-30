package com.example

import com.example.model.AiBrainMode
import com.example.model.ArmorProtocol
import com.example.model.ChatMessage
import com.example.model.MessageSender
import com.example.model.SpeechStatus
import com.example.model.SystemTelemetry
import com.example.speech.RudraTextToSpeech
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RudraUnitTest {

    @Test
    fun testVoiceProfilesConfigured() {
        val profiles = RudraTextToSpeech.DEFAULT_PROFILES
        assertTrue("Should have at least one male voice profile", profiles.isNotEmpty())

        val prime = profiles.first()
        assertEquals("jarvis_baritone", prime.id)
        assertTrue("Pitch should be in baritone male range (< 1.0)", prime.pitch < 1.0f)
    }

    @Test
    fun testChatMessageCreation() {
        val msg = ChatMessage(
            sender = MessageSender.RUDRA,
            text = "All systems online, Sir."
        )
        assertEquals(MessageSender.RUDRA, msg.sender)
        assertEquals("All systems online, Sir.", msg.text)
        assertTrue(msg.timestamp > 0)
    }

    @Test
    fun testSpeechStatusEnum() {
        val statuses = SpeechStatus.entries
        assertTrue(statuses.contains(SpeechStatus.STANDBY))
        assertTrue(statuses.contains(SpeechStatus.LISTENING))
        assertTrue(statuses.contains(SpeechStatus.PROCESSING))
        assertTrue(statuses.contains(SpeechStatus.SPEAKING))
    }

    @Test
    fun testSystemTelemetryDefaults() {
        val telemetry = SystemTelemetry(
            batteryPercent = 88,
            isCharging = true,
            availableMemoryMb = 2048,
            totalMemoryMb = 4096,
            isNetworkConnected = true,
            networkType = "WIFI"
        )
        assertEquals(88, telemetry.batteryPercent)
        assertTrue(telemetry.isCharging)
        assertTrue(telemetry.isNetworkConnected)
    }

    @Test
    fun testArmorProtocolsConfigured() {
        val protocols = ArmorProtocol.entries
        assertTrue(protocols.contains(ArmorProtocol.HOUSE_PARTY))
        assertTrue(protocols.contains(ArmorProtocol.MARK_85))
        assertTrue(protocols.contains(ArmorProtocol.HULKBUSTER))
        assertTrue(protocols.contains(ArmorProtocol.CLEAN_SLATE))
        assertTrue(protocols.contains(ArmorProtocol.SENTRY_MODE))

        val houseParty = ArmorProtocol.HOUSE_PARTY
        assertNotNull(houseParty.quote)
        assertTrue(houseParty.quote.contains("Sir"))
    }

    @Test
    fun testAiBrainModesConfigured() {
        val modes = AiBrainMode.entries
        assertTrue(modes.contains(AiBrainMode.JARVIS))
        assertTrue(modes.contains(AiBrainMode.CHATGPT_RESEARCH))
        assertTrue(modes.contains(AiBrainMode.CLAUDE_CODING))
        assertTrue(modes.contains(AiBrainMode.DEEPSEEK_REASONING))
        assertTrue(modes.contains(AiBrainMode.PERPLEXITY_SEARCH))
        assertTrue(modes.contains(AiBrainMode.CREATIVE_PROMPTER))
        assertEquals(6, modes.size)
    }
}

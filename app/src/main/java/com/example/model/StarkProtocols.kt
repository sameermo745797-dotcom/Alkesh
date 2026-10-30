package com.example.model

enum class ArmorProtocol(
    val codeName: String,
    val description: String,
    val quote: String
) {
    HOUSE_PARTY("HOUSE PARTY PROTOCOL", "Deploy all autonomous tactical iron suits", "House Party Protocol initiated, Sir. All suits converging on your telemetry."),
    MARK_85("MARK 85 NANO ARMOR", "Engage cutting-edge nanotech energy shielding", "Mark 85 Nanotech lattice active. Energy shields at 100 percent, Sir."),
    HULKBUSTER("VERONICA HULKBUSTER", "Deploy orbital satellite heavy armor pod", "Veronica telemetry confirmed. Hulkbuster orbital containment deployed."),
    CLEAN_SLATE("CLEAN SLATE PROTOCOL", "Detonate armor units in a celebratory fireworks show", "Clean slate protocol executed. A lovely sight, if I may say so, Sir."),
    SENTRY_MODE("TACTICAL SENTRY MODE", "Autonomous defensive perimeter scan", "Perimeter sentry grid established. Threat level currently zero.")
}

data class DiagnosticItem(
    val systemName: String,
    val status: String,
    val value: String,
    val isNominal: Boolean
)
